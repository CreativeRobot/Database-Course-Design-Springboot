package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.CreateBookBundleDTO;
import com.example.demo.dto.UpdateBookBundleDTO;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookBundle;
import com.example.demo.entity.BookBundleItem;
import com.example.demo.entity.BookBundleStatus;
import com.example.demo.entity.BookStatus;
import com.example.demo.repository.BookBundleItemRepository;
import com.example.demo.repository.BookBundleRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.vo.BookBundleItemVo;
import com.example.demo.vo.BookBundleVo;
import com.example.demo.vo.CartBundleVo;
import com.example.demo.vo.CustomerBookBundleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BookBundleService {
    private static final int MIN_BOOKS = 2;
    private static final int MAX_BOOKS = 10;

    @Autowired private BookBundleRepository bundleRepository;
    @Autowired private BookBundleItemRepository itemRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private BookPromotionService bookPromotionService;

    @Transactional(readOnly = true)
    public List<BookBundleVo> listAdmin() {
        return bundleRepository.findAllByOrderByIdAsc().stream().map(this::toAdminVo).toList();
    }

    @Transactional(readOnly = true)
    public BookBundleVo getAdmin(Long id) {
        return toAdminVo(getBundle(id));
    }

    @Transactional
    public BookBundleVo create(CreateBookBundleDTO dto) {
        validatePayload(dto == null ? null : dto.getName(), dto == null ? null : dto.getDescription(),
                dto == null ? null : dto.getBundlePrice(), dto == null ? null : dto.getBookIds());
        List<Book> books = resolveBooks(dto.getBookIds());
        validatePrice(dto.getBundlePrice(), books);
        BookBundle bundle = BookBundle.builder()
                .name(dto.getName().trim()).description(trimToNull(dto.getDescription()))
                .bundlePrice(money(dto.getBundlePrice())).status(BookBundleStatus.ACTIVE).version(0L).build();
        bundle = bundleRepository.save(bundle);
        saveItems(bundle, books);
        return toAdminVo(bundle);
    }

    @Transactional
    public BookBundleVo update(Long id, UpdateBookBundleDTO dto) {
        validatePayload(dto == null ? null : dto.getName(), dto == null ? null : dto.getDescription(),
                dto == null ? null : dto.getBundlePrice(), dto == null ? null : dto.getBookIds());
        if (dto == null || dto.getVersion() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "版本号不能为空");
        }
        BookBundle bundle = bundleRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "组合包不存在"));
        if (!dto.getVersion().equals(bundle.getVersion())) {
            throw new BusinessException(HttpStatus.CONFLICT, "组合包已被其他管理员修改，请刷新后重试");
        }
        List<Book> books = resolveBooks(dto.getBookIds());
        validatePrice(dto.getBundlePrice(), books);
        bundle.setName(dto.getName().trim());
        bundle.setDescription(trimToNull(dto.getDescription()));
        bundle.setBundlePrice(money(dto.getBundlePrice()));
        itemRepository.deleteByBundle_Id(id);
        saveItems(bundle, books);
        return toAdminVo(bundleRepository.save(bundle));
    }

    @Transactional
    public BookBundleVo changeStatus(Long id, BookBundleStatus status) {
        if (status == null) throw new BusinessException(HttpStatus.BAD_REQUEST, "状态不能为空");
        BookBundle bundle = bundleRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "组合包不存在"));
        if (status == BookBundleStatus.ACTIVE) {
            List<Book> books = itemRepository.findByBundle_IdOrderByBook_IdAsc(id).stream()
                    .map(BookBundleItem::getBook).toList();
            validatePrice(bundle.getBundlePrice(), books);
        }
        bundle.setStatus(status);
        return toAdminVo(bundleRepository.save(bundle));
    }

    @Transactional(readOnly = true)
    public List<CustomerBookBundleVo> listForBook(Long bookId) {
        if (bookId == null || !bookRepository.existsById(bookId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "图书不存在");
        }
        return itemRepository.findByBook_IdOrderByBundle_IdAsc(bookId).stream()
                .map(BookBundleItem::getBundle).distinct().filter(this::isCustomerVisible)
                .map(this::toCustomerVo).toList();
    }

    public List<BookBundleItem> items(Long bundleId) {
        return itemRepository.findByBundle_IdOrderByBook_IdAsc(bundleId);
    }

    /** 返回当前仍可参与用户端匹配的组合包。 */
    @Transactional(readOnly = true)
    public List<BookBundle> listCustomerBundles() {
        return bundleRepository.findByStatusOrderByIdAsc(BookBundleStatus.ACTIVE).stream()
                .filter(this::isCustomerVisible)
                .toList();
    }

    public BookBundle getBundle(Long id) {
        if (id == null) throw new BusinessException(HttpStatus.BAD_REQUEST, "组合包不能为空");
        return bundleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "组合包不存在"));
    }

    public BundlePricingService.BundleCandidate toCandidate(BookBundle bundle) {
        List<BundlePricingService.BundleMember> members = items(bundle.getId()).stream()
                .map(item -> new BundlePricingService.BundleMember(
                        item.getBook().getId(), item.getBook().getTitle(), item.getBook().getCoverUrl(),
                        money(bookPromotionService.effectivePrice(item.getBook()))))
                .toList();
        return new BundlePricingService.BundleCandidate(bundle.getId(), bundle.getName(),
                money(bundle.getBundlePrice()), members);
    }

    public boolean isCustomerVisible(BookBundle bundle) {
        if (bundle == null || bundle.getStatus() != BookBundleStatus.ACTIVE) return false;
        List<Book> books = items(bundle.getId()).stream().map(BookBundleItem::getBook).toList();
        if (books.size() < MIN_BOOKS || books.size() > MAX_BOOKS) return false;
        if (books.stream().anyMatch(book -> book.getStatus() != BookStatus.ON_SALE || book.getStock() == null || book.getStock() < 1)) {
            return false;
        }
        return isPriceValid(bundle.getBundlePrice(), books);
    }

    public CartBundleVo toCartVo(BundlePricingService.BundleCandidate candidate, boolean applied) {
        CartBundleVo vo = new CartBundleVo();
        vo.setId(candidate.id()); vo.setName(candidate.name()); vo.setBundlePrice(candidate.bundlePrice());
        BigDecimal regular = candidate.members().stream().map(BundlePricingService.BundleMember::salePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        vo.setRegularAmount(regular); vo.setSavings(regular.subtract(candidate.bundlePrice()).setScale(2, RoundingMode.HALF_UP));
        vo.setItems(candidate.members().stream().map(member -> {
            BookBundleItemVo item = new BookBundleItemVo(); item.setBookId(member.bookId()); item.setTitle(member.title());
            item.setCoverUrl(member.coverUrl()); item.setSalePrice(member.salePrice()); return item;
        }).toList()); vo.setApplied(applied); return vo;
    }

    public CustomerBookBundleVo toCustomerVo(BookBundle bundle) {
        List<BookBundleItem> items = items(bundle.getId());
        BigDecimal regular = items.stream().map(item -> bookPromotionService.effectivePrice(item.getBook()))
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        CustomerBookBundleVo vo = new CustomerBookBundleVo(); vo.setId(bundle.getId()); vo.setName(bundle.getName());
        vo.setDescription(bundle.getDescription()); vo.setBundlePrice(money(bundle.getBundlePrice())); vo.setRegularAmount(regular);
        vo.setSavings(regular.subtract(bundle.getBundlePrice()).setScale(2, RoundingMode.HALF_UP));
        vo.setItems(items.stream().map(this::toItemVo).toList()); return vo;
    }

    private BookBundleVo toAdminVo(BookBundle bundle) {
        List<BookBundleItem> members = items(bundle.getId());
        List<Book> books = members.stream().map(BookBundleItem::getBook).toList();
        BigDecimal regular = books.stream().map(bookPromotionService::effectivePrice).filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        boolean priceValid = isPriceValid(bundle.getBundlePrice(), books);
        String reason = null;
        if (members.size() < MIN_BOOKS || members.size() > MAX_BOOKS) reason = "组合成员数量必须在2到10本之间";
        else if (!priceValid) reason = "组合价不低于当前图书售价合计";
        else if (books.stream().anyMatch(book -> book.getStatus() != BookStatus.ON_SALE)) reason = "存在下架图书";
        else if (books.stream().anyMatch(book -> book.getStock() == null || book.getStock() < 1)) reason = "存在缺货图书";
        BookBundleVo vo = new BookBundleVo(); vo.setId(bundle.getId()); vo.setName(bundle.getName()); vo.setDescription(bundle.getDescription());
        vo.setBundlePrice(money(bundle.getBundlePrice())); vo.setRegularAmount(regular); vo.setSavings(regular.subtract(bundle.getBundlePrice()).setScale(2, RoundingMode.HALF_UP));
        vo.setStatus(bundle.getStatus()); vo.setVersion(bundle.getVersion()); vo.setPriceValid(priceValid); vo.setPurchasable(isCustomerVisible(bundle)); vo.setUnavailableReason(reason);
        vo.setItems(members.stream().map(this::toItemVo).toList()); vo.setCreateTime(bundle.getCreateTime()); vo.setUpdateTime(bundle.getUpdateTime()); return vo;
    }

    private BookBundleItemVo toItemVo(BookBundleItem relation) {
        Book book = relation.getBook(); BookBundleItemVo vo = new BookBundleItemVo(); vo.setBookId(book.getId()); vo.setTitle(book.getTitle());
        vo.setIsbn(book.getIsbn()); vo.setCoverUrl(book.getCoverUrl()); vo.setSalePrice(money(bookPromotionService.effectivePrice(book))); vo.setStock(book.getStock()); vo.setBookStatus(book.getStatus().name()); return vo;
    }

    private List<Book> resolveBooks(List<Long> ids) {
        Set<Long> unique = new LinkedHashSet<>(ids == null ? List.of() : ids);
        if (unique.size() != (ids == null ? 0 : ids.size())) throw new BusinessException(HttpStatus.BAD_REQUEST, "组合包不能包含重复图书");
        List<Book> books = bookRepository.findAllById(unique);
        if (books.size() != unique.size()) throw new BusinessException(HttpStatus.BAD_REQUEST, "组合包包含不存在的图书");
        return unique.stream().map(id -> books.stream().filter(book -> book.getId().equals(id)).findFirst().orElseThrow()).toList();
    }

    private void saveItems(BookBundle bundle, List<Book> books) {
        itemRepository.saveAll(books.stream().map(book -> BookBundleItem.builder().bundle(bundle).book(book).build()).toList());
    }

    private void validatePayload(String name, String description, BigDecimal price, List<Long> bookIds) {
        if (!StringUtils.hasText(name)) throw new BusinessException(HttpStatus.BAD_REQUEST, "组合包名称不能为空");
        if (name.trim().length() > 100) throw new BusinessException(HttpStatus.BAD_REQUEST, "组合包名称不能超过100个字符");
        if (description != null && description.length() > 500) throw new BusinessException(HttpStatus.BAD_REQUEST, "组合包说明不能超过500个字符");
        if (price == null || price.signum() < 0 || price.scale() > 2) throw new BusinessException(HttpStatus.BAD_REQUEST, "组合包价格必须为两位小数以内的非负数");
        if (bookIds == null || bookIds.size() < MIN_BOOKS || bookIds.size() > MAX_BOOKS) throw new BusinessException(HttpStatus.BAD_REQUEST, "组合包必须包含2到10本图书");
    }

    private void validatePrice(BigDecimal price, List<Book> books) {
        if (!isPriceValid(price, books)) throw new BusinessException(HttpStatus.BAD_REQUEST, "组合价必须低于成员图书当前售价合计");
    }

    private boolean isPriceValid(BigDecimal price, List<Book> books) {
        if (price == null || books == null || books.isEmpty() || books.stream().anyMatch(book -> bookPromotionService.effectivePrice(book) == null)) return false;
        BigDecimal regular = books.stream().map(bookPromotionService::effectivePrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        return money(price).compareTo(money(regular)) < 0;
    }

    private static BigDecimal money(BigDecimal value) { return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP); }
    private static String trimToNull(String value) { return StringUtils.hasText(value) ? value.trim() : null; }
}
