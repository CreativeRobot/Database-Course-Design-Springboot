package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.AddCartItemDTO;
import com.example.demo.dto.UpdateCartItemDTO;
import com.example.demo.dto.UpdateCartSelectionDTO;
import com.example.demo.service.CartService;
import com.example.demo.vo.CartItemVo;
import com.example.demo.vo.CartVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前用户购物车接口。
 * 路径位于 /api/cart/** 下，经过 JwtInterceptor 后从请求属性中取得 userId。
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /** 查询完整购物车及汇总信息。 */
    @GetMapping
    public Result<CartVo> getCart(@RequestAttribute("userId") Long userId) {
        return Result.success(cartService.getCart(userId));
    }

    /** 查询已选中的购物车商品及汇总信息。 */
    @GetMapping("/selected")
    public Result<CartVo> getSelectedCart(@RequestAttribute("userId") Long userId) {
        return Result.success(cartService.getSelectedCart(userId));
    }

    /** 加入购物车；已有同种图书时累加数量。 */
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<CartItemVo> addItem(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody AddCartItemDTO dto) {
        return Result.success(cartService.addItem(userId, dto));
    }

    /** 按图书编号修改购买数量或选中状态。 */
    @PutMapping("/items/{bookId}")
    public Result<CartItemVo> updateItem(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateCartItemDTO dto) {
        return Result.success(cartService.updateItem(userId, bookId, dto));
    }

    /** 全选或取消全选购物车。 */
    @PutMapping("/selection")
    public Result<CartVo> updateAllSelection(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UpdateCartSelectionDTO dto) {
        return Result.success(cartService.updateAllSelection(userId, dto.getSelected()));
    }

    /** 按图书编号删除一条购物车商品。 */
    @DeleteMapping("/items/{bookId}")
    public Result<Void> removeItem(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long bookId) {
        cartService.removeItem(userId, bookId);
        return Result.success(null);
    }

    /** 清理全部已选商品，响应数据为删除数量。 */
    @DeleteMapping("/selected")
    public Result<Long> removeSelectedItems(
            @RequestAttribute("userId") Long userId) {
        return Result.success(cartService.removeSelectedItems(userId));
    }
}
