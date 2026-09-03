package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.AddCartItemDTO;
import com.example.demo.service.BookBundleService;
import com.example.demo.service.CartService;
import com.example.demo.vo.CartItemVo;
import com.example.demo.vo.CartVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** 购物车组合包快捷加入接口。 */
@RestController
@RequestMapping("/api/cart/bundles")
public class CartBundleController {
    @Autowired private CartService cartService;
    @Autowired private BookBundleService bookBundleService;

    @PostMapping("/{bundleId}")
    public Result<CartVo> addBundle(@RequestAttribute("userId") Long userId,
                                    @PathVariable Long bundleId) {
        return Result.success(cartService.addBundle(userId, bookBundleService.getBundle(bundleId)));
    }
}
