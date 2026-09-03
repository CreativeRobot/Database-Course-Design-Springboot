package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.PromotionHomeService;
import com.example.demo.vo.PromotionHomeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {
    @Autowired private PromotionHomeService promotionHomeService;
    @GetMapping("/home")
    public Result<PromotionHomeVo> home(@RequestParam(defaultValue = "8") int limit) { return Result.success(promotionHomeService.home(limit)); }
}
