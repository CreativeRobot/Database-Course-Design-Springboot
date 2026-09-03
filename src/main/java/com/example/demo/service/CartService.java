package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.AddCartItemDTO;
import com.example.demo.dto.UpdateCartItemDTO;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookStatus;
import com.example.demo.entity.BookBundle;
import com.example.demo.entity.CartItem;
import com.example.demo.entity.User;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.CartItemVo;
import com.example.demo.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * 当前登录用户的购物车业务。
 * 购物车只保存购买意向，不预占库存；加入或修改数量时按图书当前库存进行校验。
 */
@Service
public class CartService {

    private static final int MAX_ITEM_QUANTITY = 999;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookBundleService bookBundleService;

    @Autowired
    private BundlePricingService bundlePricingService;

    @Autowired
    private BookPromotionService bookPromotionService;

    // ==================== 购物车查询 ====================

    /** 查询当前用户的全部购物车商品，并返回数量与已选金额汇总。 */
    @Transactional(readOnly = true)
    public CartVo getCart(Long userId) {
        getActiveUser(userId);
        return buildCart(cartItemRepository.findByUser_IdOrderByCreateTimeDesc(userId));
    }

    /** 查询当前用户已选中的购物车商品，供订单确认等流程使用。 */
    @Transactional(readOnly = true)
    public CartVo getSelectedCart(Long userId) {
        getActiveUser(userId);
        return buildCart(cartItemRepository
                .findByUser_IdAndSelectedTrueOrderByCreateTimeDesc(userId));
    }

    // ==================== 购物车写操作 ====================

    /**
     * 将图书加入购物车。
     * 同一本图书已存在时累加数量，并重新设为选中状态。
     */
    @Transactional
    public CartItemVo addItem(Long userId, AddCartItemDTO dto) {
        if (dto == null || dto.getBookId() == null || dto.getQuantity() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "图书和购买数量不能为空");
        }

        User user = getActiveUser(userId);
        Book book = getPurchasableBook(dto.getBookId());
        validateQuantity(dto.getQuantity(), book.getStock());

        CartItem item = cartItemRepository.findByUser_IdAndBook_Id(userId, book.getId())
                .orElse(null);
        if (item == null) {
            item = CartItem.builder()
                    .user(user)
                    .book(book)
                    .quantity(dto.getQuantity())
                    .selected(true)
                    .build();
        } else {
            long mergedQuantity = (long) item.getQuantity() + dto.getQuantity();
            if (mergedQuantity > MAX_ITEM_QUANTITY) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "单种图书最多购买" + MAX_ITEM_QUANTITY + "本");
            }
            validateQuantity((int) mergedQuantity, book.getStock());
            item.setQuantity((int) mergedQuantity);
            item.setSelected(true);
        }
        return toItemVo(cartItemRepository.save(item));
    }

    /** 将组合包内每本图书各加入一份；组合优惠在购物车/下单时统一匹配。 */
    @Transactional
    public CartVo addBundle(Long userId, BookBundle bundle) {
        getActiveUser(userId);
        if (bundle == null || !bookBundleService.isCustomerVisible(bundle)) {
            throw new BusinessException(HttpStatus.CONFLICT, "组合包当前不可购买");
        }
        for (var relation : bookBundleService.items(bundle.getId())) {
            Book book = getPurchasableBook(relation.getBook().getId());
            CartItem item = cartItemRepository.findByUser_IdAndBook_Id(userId, book.getId()).orElse(null);
            int current = item == null || item.getQuantity() == null ? 0 : item.getQuantity();
            if (current >= MAX_ITEM_QUANTITY || current + 1 > book.getStock()) {
                throw new BusinessException(HttpStatus.CONFLICT, "图书《" + book.getTitle() + "》库存不足");
            }
            if (item == null) {
                item = CartItem.builder().user(userRepository.getReferenceById(userId)).book(book)
                        .quantity(1).selected(true).build();
            } else {
                item.setQuantity(current + 1);
                item.setSelected(true);
            }
            cartItemRepository.save(item);
        }
        return getCart(userId);
    }

    /**
     * 修改指定图书在购物车中的数量或选中状态。
     * quantity、selected 至少需要传入一个，未传字段保持不变。
     */
    @Transactional
    public CartItemVo updateItem(Long userId, Long bookId, UpdateCartItemDTO dto) {
        getActiveUser(userId);
        if (dto == null || (dto.getQuantity() == null && dto.getSelected() == null)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "至少需要修改一个购物车字段");
        }

        CartItem item = getCartItem(userId, bookId);
        if (dto.getQuantity() != null) {
            Book book = getPurchasableBook(item.getBook().getId());
            validateQuantity(dto.getQuantity(), book.getStock());
            item.setQuantity(dto.getQuantity());
        }
        if (dto.getSelected() != null) {
            item.setSelected(dto.getSelected());
        }
        return toItemVo(cartItemRepository.save(item));
    }

    /** 全选或取消全选当前用户的购物车商品。 */
    @Transactional
    public CartVo updateAllSelection(Long userId, boolean selected) {
        getActiveUser(userId);
        List<CartItem> items = cartItemRepository.findByUser_IdOrderByCreateTimeDesc(userId);
        items.forEach(item -> item.setSelected(selected));
        return buildCart(cartItemRepository.saveAll(items));
    }

    /** 按图书编号删除当前用户的一条购物车商品。 */
    @Transactional
    public void removeItem(Long userId, Long bookId) {
        getActiveUser(userId);
        if (bookId == null
                || cartItemRepository.deleteByUser_IdAndBook_Id(userId, bookId) == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "购物车商品不存在");
        }
    }

    /** 删除当前用户全部已选商品，返回实际删除数量。 */
    @Transactional
    public long removeSelectedItems(Long userId) {
        getActiveUser(userId);
        return cartItemRepository.deleteByUser_IdAndSelectedTrue(userId);
    }

    // ==================== 私有辅助方法 ====================

    /** 按用户和图书联合定位购物车商品，防止跨用户操作。 */
    private CartItem getCartItem(Long userId, Long bookId) {
        if (bookId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "图书不能为空");
        }
        return cartItemRepository.findByUser_IdAndBook_Id(userId, bookId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "购物车商品不存在"));
    }

    /** 查询可购买图书，并校验在售状态和基础库存。 */
    private Book getPurchasableBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "图书不存在"));
        if (book.getStatus() != BookStatus.ON_SALE) {
            throw new BusinessException(HttpStatus.CONFLICT, "图书已下架，无法加入购物车");
        }
        if (book.getStock() == null || book.getStock() <= 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "图书库存不足");
        }
        return book;
    }

    /** 校验用户仍然存在且账号处于启用状态。 */
    private User getActiveUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或Token已失效");
        }
        return userRepository.findByIdAndStatus(userId, 1)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "用户不存在或已被禁用"));
    }

    /** 校验单种图书的购买数量上限及当前库存。 */
    private void validateQuantity(int quantity, int stock) {
        if (quantity < 1 || quantity > MAX_ITEM_QUANTITY) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "购买数量必须在1到" + MAX_ITEM_QUANTITY + "之间");
        }
        if (quantity > stock) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "图书库存不足，当前库存为" + stock);
        }
    }

    /** 将购物车明细转换为前端视图，并计算汇总数据。 */
    private CartVo buildCart(List<CartItem> items) {
        List<CartItemVo> itemVos = items.stream().map(this::toItemVo).toList();
        int totalQuantity = itemVos.stream()
                .mapToInt(CartItemVo::getQuantity)
                .sum();
        int selectedQuantity = itemVos.stream()
                .filter(CartItemVo::getSelected)
                .mapToInt(CartItemVo::getQuantity)
                .sum();
        BigDecimal selectedAmount = itemVos.stream()
                .filter(CartItemVo::getSelected)
                .map(CartItemVo::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartVo vo = new CartVo();
        vo.setItems(itemVos);
        vo.setTotalQuantity(totalQuantity);
        vo.setSelectedQuantity(selectedQuantity);
        vo.setSelectedAmount(selectedAmount);
        vo.setRegularAmount(selectedAmount);
        BundlePricingService.PricingResult pricing = priceSelectedItems(items);
        vo.setBundleDiscountAmount(pricing.discountAmount());
        vo.setPayableAmount(pricing.payableAmount());
        Set<Long> appliedIds = Set.copyOf(pricing.selectedBundleIds());
        vo.setEligibleBundles(pricing.eligibleBundles().stream()
                .map(candidate -> bookBundleService.toCartVo(candidate, appliedIds.contains(candidate.id())))
                .toList());
        vo.setAppliedBundles(pricing.selectedBundleIds().stream()
                .map(id -> pricing.eligibleBundles().stream()
                        .filter(bundle -> bundle.id().equals(id)).findFirst().orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(candidate -> bookBundleService.toCartVo(candidate, true)).toList());
        return vo;
    }

    private BundlePricingService.PricingResult priceSelectedItems(List<CartItem> items) {
        Map<Long, BundlePricingService.CartBook> cartBooks = new HashMap<>();
        for (CartItem item : items) {
            if (!Boolean.TRUE.equals(item.getSelected()) || item.getBook() == null || item.getQuantity() == null) continue;
            Book book = item.getBook();
            cartBooks.put(book.getId(), new BundlePricingService.CartBook(
                    book.getId(), item.getQuantity(), bookPromotionService.effectivePrice(book)));
        }
        List<BundlePricingService.BundleCandidate> candidates = bookBundleService.listCustomerBundles().stream()
                .map(bookBundleService::toCandidate).toList();
        return bundlePricingService.price(cartBooks, candidates);
    }

    /** 转换单条购物车数据，并标记当前是否仍可购买。 */
    private CartItemVo toItemVo(CartItem item) {
        Book book = item.getBook();
        CartItemVo vo = new CartItemVo();
        vo.setId(item.getId());
        vo.setBookId(book.getId());
        vo.setIsbn(book.getIsbn());
        vo.setTitle(book.getTitle());
        vo.setCoverUrl(book.getCoverUrl());
        vo.setSalePrice(bookPromotionService.effectivePrice(book));
        vo.setStock(book.getStock());
        vo.setBookStatus(book.getStatus());
        vo.setPreSale(BookPreSalePolicy.isActive(book.getPreSale(), book.getPreSaleReleaseTime(), java.time.LocalDateTime.now()));
        vo.setPreSaleReleaseTime(book.getPreSaleReleaseTime());
        vo.setQuantity(item.getQuantity());
        vo.setSelected(item.getSelected());
        vo.setAvailable(book.getStatus() == BookStatus.ON_SALE
                && book.getStock() != null
                && book.getStock() >= item.getQuantity());
        vo.setSubtotal(bookPromotionService.effectivePrice(book).multiply(BigDecimal.valueOf(item.getQuantity())));
        vo.setCreateTime(item.getCreateTime());
        vo.setUpdateTime(item.getUpdateTime());
        return vo;
    }
}
