package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.BookCreateDTO;
import com.example.demo.dto.BookUpdateDTO;
import com.example.demo.dto.StockAdjustDTO;
import com.example.demo.entity.Author;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookAuthor;
import com.example.demo.entity.BookAuthorId;
import com.example.demo.entity.BookCategory;
import com.example.demo.entity.BookCategoryId;
import com.example.demo.entity.BookStatus;
import com.example.demo.entity.Category;
import com.example.demo.entity.InventoryChangeType;
import com.example.demo.entity.InventoryLog;
import com.example.demo.entity.Publisher;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.BookAuthorRepository;
import com.example.demo.repository.BookCategoryRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.InventoryLogRepository;
import com.example.demo.repository.PublisherRepository;
import com.example.demo.vo.BookDetailVo;
import com.example.demo.vo.BookVo;
import com.example.demo.vo.PageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookService {

    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookAuthorRepository bookAuthorRepository;

    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    @Autowired
    private InventoryLogRepository inventoryLogRepository;

    @Autowired
    private RecommendationService recommendationService;

    // ==================== 公开查询（仅在售图书） ====================

    /**
     * 分页查询在售图书。
     * keyword、categoryId、authorId、publisherId 为可选过滤条件，最多同时生效一个，
     * 优先级：keyword > categoryId > authorId > publisherId。
     */
    @Transactional(readOnly = true)
    public PageVo<BookVo> listOnSaleBooks(
            String keyword, Long categoryId, Long authorId, Long publisherId,
            java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice,
            boolean inStock, String sortBy, String direction, int page, int size) {
        validatePriceRange(minPrice, maxPrice);
        Pageable pageable = buildSearchPageable(page, size, sortBy, direction);
        Specification<Book> spec = (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), BookStatus.ON_SALE));
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), pattern));
            }
            if (categoryId != null) predicates.add(existsCategory(query, cb, root, categoryId));
            if (authorId != null) predicates.add(existsAuthor(query, cb, root, authorId));
            if (publisherId != null) predicates.add(cb.equal(root.get("publisher").get("id"), publisherId));
            if (minPrice != null) predicates.add(cb.greaterThanOrEqualTo(root.get("salePrice"), minPrice));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(root.get("salePrice"), maxPrice));
            if (inStock) predicates.add(cb.greaterThan(root.get("stock"), 0));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return PageVo.of(bookRepository.findAll(spec, pageable).map(this::toBookVo));
    }

    private Predicate existsAuthor(CriteriaQuery<?> query, CriteriaBuilder cb, Root<Book> root, Long authorId) {
        var subquery = query.subquery(Long.class);
        var relation = subquery.from(BookAuthor.class);
        subquery.select(cb.literal(1L));
        subquery.where(cb.equal(relation.get("book"), root), cb.equal(relation.get("author").get("id"), authorId));
        return cb.exists(subquery);
    }

    private Predicate existsCategory(CriteriaQuery<?> query, CriteriaBuilder cb, Root<Book> root, Long categoryId) {
        var subquery = query.subquery(Long.class);
        var relation = subquery.from(BookCategory.class);
        subquery.select(cb.literal(1L));
        subquery.where(cb.equal(relation.get("book"), root), cb.equal(relation.get("category").get("id"), categoryId));
        return cb.exists(subquery);
    }

    private Pageable buildSearchPageable(int page, int size, String sortBy, String direction) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分页参数不合法");
        }
        Sort.Direction order = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String normalizedSort = sortBy == null ? "latest" : sortBy.toLowerCase();
        String property = switch (normalizedSort) {
            case "price" -> "salePrice";
            case "sales" -> "salesCount";
            default -> "createTime";
        };
        return PageRequest.of(page - 1, size, Sort.by(order, property).and(Sort.by(Sort.Direction.DESC, "id")));
    }

    private void validatePriceRange(java.math.BigDecimal min, java.math.BigDecimal max) {
        if (min != null && min.signum() < 0 || max != null && max.signum() < 0 || min != null && max != null && min.compareTo(max) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "价格区间不合法");
        }
    }

    /** 查询在售图书详情（顾客视角，下架图书返回404） */
    @Transactional(readOnly = true)
    public BookDetailVo getOnSaleBookDetail(Long bookId) {
        Book book = getBookOrThrow(bookId);
        if (book.getStatus() != BookStatus.ON_SALE) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "图书不存在或已下架");
        }
        return toDetailVo(book);
    }

    // ==================== 管理端查询 ====================

    /** 管理端分页查询全部图书（含下架） */
    @Transactional(readOnly = true)
    public PageVo<BookVo> listAllBooks(BookStatus status, int page, int size) {
        Pageable pageable = buildPageable(page, size);
        Page<Book> books = status == null
                ? bookRepository.findAll(pageable)
                : bookRepository.findByStatus(status, pageable);
        return PageVo.of(books.map(this::toBookVo));
    }

    /** 管理端查询图书详情（含下架图书） */
    @Transactional(readOnly = true)
    public BookDetailVo getBookDetail(Long bookId) {
        return toDetailVo(getBookOrThrow(bookId));
    }

    /** 查询库存不高于阈值的在售图书，用于补货提醒 */
    @Transactional(readOnly = true)
    public List<BookVo> listLowStockBooks(int threshold) {
        if (threshold < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "库存阈值不能为负数");
        }
        return bookRepository
                .findByStockLessThanEqualAndStatusOrderByStockAsc(threshold, BookStatus.ON_SALE)
                .stream()
                .map(this::toBookVo)
                .toList();
    }

    // ==================== 管理端写操作 ====================

    @Transactional
    public BookDetailVo createBook(BookCreateDTO dto) {
        validateCreateRequest(dto);
        String isbn = dto.getIsbn().trim();
        if (bookRepository.existsByIsbn(isbn)) {
            throw new BusinessException(HttpStatus.CONFLICT, "该ISBN已存在");
        }
        validatePrices(dto.getOriginalPrice(), dto.getSalePrice());

        Publisher publisher = publisherRepository.findById(dto.getPublisherId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "出版社不存在"));

        List<Author> authors = findAuthorsOrThrow(dto.getAuthorIds());
        List<Category> categories = findCategoriesOrThrow(dto.getCategoryIds());

        int initialStock = dto.getStock() == null ? 0 : dto.getStock();

        Book book = Book.builder()
                .isbn(isbn)
                .title(dto.getTitle().trim())
                .publisher(publisher)
                .originalPrice(dto.getOriginalPrice())
                .salePrice(dto.getSalePrice())
                .stock(initialStock)
                .publishDate(dto.getPublishDate())
                .edition(trimToNull(dto.getEdition()))
                .pages(dto.getPages())
                .description(trimToNull(dto.getDescription()))
                .coverUrl(trimToNull(dto.getCoverUrl()))
                .status(BookStatus.ON_SALE)
                .build();
        book = bookRepository.save(book);

        saveAuthorRelations(book, authors);
        saveCategoryRelations(book, categories);

        // 初始库存计入流水，保证库存可追溯
        if (initialStock > 0) {
            recordInventoryLog(book, initialStock, 0, initialStock,
                    InventoryChangeType.PURCHASE_IN, "新书入库");
        }

        recommendationService.invalidateAllAfterCommit();

        return toDetailVo(book);
    }

    @Transactional
    public BookDetailVo updateBook(Long bookId, BookUpdateDTO dto) {
        validateUpdateRequest(dto);
        Book book = getBookOrThrow(bookId);

        if (StringUtils.hasText(dto.getIsbn())) {
            String isbn = dto.getIsbn().trim();
            if (!isbn.equals(book.getIsbn()) && bookRepository.existsByIsbn(isbn)) {
                throw new BusinessException(HttpStatus.CONFLICT, "该ISBN已存在");
            }
            book.setIsbn(isbn);
        }
        if (StringUtils.hasText(dto.getTitle())) {
            book.setTitle(dto.getTitle().trim());
        }
        if (dto.getPublisherId() != null) {
            Publisher publisher = publisherRepository.findById(dto.getPublisherId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "出版社不存在"));
            book.setPublisher(publisher);
        }

        BigDecimalPair prices = resolvePrices(book, dto);
        validatePrices(prices.original(), prices.sale());
        book.setOriginalPrice(prices.original());
        book.setSalePrice(prices.sale());

        if (dto.getPublishDate() != null) {
            book.setPublishDate(dto.getPublishDate());
        }
        if (dto.getEdition() != null) {
            book.setEdition(trimToNull(dto.getEdition()));
        }
        if (dto.getPages() != null) {
            book.setPages(dto.getPages());
        }
        if (dto.getDescription() != null) {
            book.setDescription(trimToNull(dto.getDescription()));
        }
        if (dto.getCoverUrl() != null) {
            book.setCoverUrl(trimToNull(dto.getCoverUrl()));
        }

        if (dto.getAuthorIds() != null) {
            if (dto.getAuthorIds().isEmpty()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "至少需要一位作者");
            }
            List<Author> authors = findAuthorsOrThrow(dto.getAuthorIds());
            bookAuthorRepository.deleteByBook_Id(book.getId());
            // Hibernate 同一事务内 INSERT 先于 DELETE 执行，必须先 flush 删除，否则相同复合主键会冲突
            bookAuthorRepository.flush();
            saveAuthorRelations(book, authors);
        }
        if (dto.getCategoryIds() != null) {
            List<Category> categories = findCategoriesOrThrow(dto.getCategoryIds());
            bookCategoryRepository.deleteByBook_Id(book.getId());
            bookCategoryRepository.flush();
            saveCategoryRelations(book, categories);
        }

        book = bookRepository.save(book);
        recommendationService.invalidateAllAfterCommit();
        return toDetailVo(book);
    }

    /** 上架/下架。下架代替物理删除，避免破坏订单、评论等历史数据。 */
    @Transactional
    public BookDetailVo changeStatus(Long bookId, BookStatus status) {
        Book book = getBookOrThrow(bookId);
        book.setStatus(status);
        book = bookRepository.save(book);
        recommendationService.invalidateAllAfterCommit();
        return toDetailVo(book);
    }

    /** 管理员手动调整库存，同时写入库存流水 */
    @Transactional
    public BookDetailVo adjustStock(Long bookId, StockAdjustDTO dto) {
        if (dto == null || dto.getChangeQuantity() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "库存变动数量不能为空");
        }
        int change = dto.getChangeQuantity();
        if (change == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "变动数量不能为0");
        }

        Book book = getBookOrThrow(bookId);
        int before = book.getStock();
        int after = before + change;
        if (after < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "库存不足，当前库存为" + before);
        }

        book.setStock(after);
        book = bookRepository.save(book);

        recordInventoryLog(book, change, before, after,
                InventoryChangeType.MANUAL_ADJUSTMENT, trimToNull(dto.getRemark()));

        recommendationService.invalidateAllAfterCommit();

        return toDetailVo(book);
    }

    // ==================== 私有辅助方法 ====================

    private void validateCreateRequest(BookCreateDTO dto) {
        if (dto == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "图书请求不能为空");
        }
        if (!StringUtils.hasText(dto.getIsbn())
                || !StringUtils.hasText(dto.getTitle())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "ISBN和书名不能为空");
        }
        if (dto.getPublisherId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "出版社不能为空");
        }
        if (dto.getAuthorIds() == null || dto.getAuthorIds().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "至少需要一位作者");
        }
        if (dto.getStock() != null && dto.getStock() < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "库存不能为负数");
        }
        if (dto.getPages() != null && dto.getPages() < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "页数必须大于0");
        }
        validatePrices(dto.getOriginalPrice(), dto.getSalePrice());
    }

    private void validateUpdateRequest(BookUpdateDTO dto) {
        if (dto == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "图书更新请求不能为空");
        }
        if (dto.getPages() != null && dto.getPages() < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "页数必须大于0");
        }
        if (dto.getAuthorIds() != null && dto.getAuthorIds().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "至少需要一位作者");
        }
    }

    private Pageable buildPageable(int page, int size) {
        if (page < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "页码必须从1开始");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "每页数量必须在1到" + MAX_PAGE_SIZE + "之间");
        }
        return PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));
    }

    private Book getBookOrThrow(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "图书不存在"));
    }

    private void validatePrices(java.math.BigDecimal originalPrice, java.math.BigDecimal salePrice) {
        if (originalPrice == null || salePrice == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "原价和售价不能为空");
        }
        if (originalPrice.signum() < 0 || salePrice.signum() < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "价格不能为负数");
        }
        if (salePrice.compareTo(originalPrice) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "售价不能高于原价");
        }
    }

    private record BigDecimalPair(java.math.BigDecimal original, java.math.BigDecimal sale) {
    }

    /** 合并更新请求与现有值，得到更新后的价格组合再统一校验 */
    private BigDecimalPair resolvePrices(Book book, BookUpdateDTO dto) {
        java.math.BigDecimal original = dto.getOriginalPrice() != null
                ? dto.getOriginalPrice() : book.getOriginalPrice();
        java.math.BigDecimal sale = dto.getSalePrice() != null
                ? dto.getSalePrice() : book.getSalePrice();
        return new BigDecimalPair(original, sale);
    }

    /** 按传入顺序查找作者，任何一个不存在都报错；去重但保留顺序 */
    private List<Author> findAuthorsOrThrow(List<Long> authorIds) {
        Set<Long> uniqueIds = new LinkedHashSet<>(authorIds);
        List<Author> authors = new ArrayList<>();
        for (Long authorId : uniqueIds) {
            if (authorId == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "作者不能为空");
            }
            authors.add(authorRepository.findById(authorId)
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.BAD_REQUEST, "作者不存在：" + authorId)));
        }
        return authors;
    }

    private List<Category> findCategoriesOrThrow(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }
        Set<Long> uniqueIds = new LinkedHashSet<>(categoryIds);
        List<Category> categories = new ArrayList<>();
        for (Long categoryId : uniqueIds) {
            if (categoryId == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "分类不能为空");
            }
            categories.add(categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.BAD_REQUEST, "分类不存在：" + categoryId)));
        }
        return categories;
    }

    private void saveAuthorRelations(Book book, List<Author> authors) {
        int order = 1;
        for (Author author : authors) {
            BookAuthor relation = BookAuthor.builder()
                    .id(new BookAuthorId(book.getId(), author.getId()))
                    .book(book)
                    .author(author)
                    .authorOrder(order++)
                    .build();
            bookAuthorRepository.save(relation);
        }
    }

    private void saveCategoryRelations(Book book, List<Category> categories) {
        for (Category category : categories) {
            BookCategory relation = BookCategory.builder()
                    .id(new BookCategoryId(book.getId(), category.getId()))
                    .book(book)
                    .category(category)
                    .build();
            bookCategoryRepository.save(relation);
        }
    }

    private void recordInventoryLog(Book book, int change, int before, int after,
                                    InventoryChangeType type, String remark) {
        InventoryLog log = InventoryLog.builder()
                .book(book)
                .changeQuantity(change)
                .beforeStock(before)
                .afterStock(after)
                .changeType(type)
                .remark(remark)
                .build();
        inventoryLogRepository.save(log);
    }

    private BookVo toBookVo(Book book) {
        BookVo vo = new BookVo();
        vo.setId(book.getId());
        vo.setIsbn(book.getIsbn());
        vo.setTitle(book.getTitle());
        vo.setPublisherId(book.getPublisher().getId());
        vo.setPublisherName(book.getPublisher().getName());
        vo.setOriginalPrice(book.getOriginalPrice());
        vo.setSalePrice(book.getSalePrice());
        vo.setStock(book.getStock());
        vo.setStatus(book.getStatus());
        vo.setCoverUrl(book.getCoverUrl());
        return vo;
    }

    private BookDetailVo toDetailVo(Book book) {
        BookDetailVo vo = new BookDetailVo();
        vo.setId(book.getId());
        vo.setIsbn(book.getIsbn());
        vo.setTitle(book.getTitle());
        vo.setPublisherId(book.getPublisher().getId());
        vo.setPublisherName(book.getPublisher().getName());
        vo.setOriginalPrice(book.getOriginalPrice());
        vo.setSalePrice(book.getSalePrice());
        vo.setStock(book.getStock());
        vo.setPublishDate(book.getPublishDate());
        vo.setEdition(book.getEdition());
        vo.setPages(book.getPages());
        vo.setDescription(book.getDescription());
        vo.setCoverUrl(book.getCoverUrl());
        vo.setStatus(book.getStatus());
        vo.setCreateTime(book.getCreateTime());
        vo.setUpdateTime(book.getUpdateTime());

        vo.setAuthors(bookAuthorRepository
                .findByBook_IdOrderByAuthorOrderAsc(book.getId())
                .stream()
                .map(relation -> new BookDetailVo.AuthorItem(
                        relation.getAuthor().getId(),
                        relation.getAuthor().getName()))
                .toList());

        vo.setCategories(bookCategoryRepository
                .findByBook_IdOrderByCategory_NameAsc(book.getId())
                .stream()
                .map(relation -> new BookDetailVo.CategoryItem(
                        relation.getCategory().getId(),
                        relation.getCategory().getName()))
                .toList());

        return vo;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
