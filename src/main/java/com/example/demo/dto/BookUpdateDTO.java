package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 图书更新请求体。
 * 所有字段均可选，为 null 的字段表示不修改。
 * 库存不在此处修改，请使用库存调整接口以保留库存流水。
 */
@Data
public class BookUpdateDTO {

    @Size(max = 20, message = "ISBN不能超过20个字符")
    private String isbn;

    @Size(max = 200, message = "书名不能超过200个字符")
    private String title;

    private Long publisherId;

    @DecimalMin(value = "0.00", message = "原价不能为负数")
    @Digits(integer = 8, fraction = 2, message = "原价格式不正确")
    private BigDecimal originalPrice;

    @DecimalMin(value = "0.00", message = "售价不能为负数")
    @Digits(integer = 8, fraction = 2, message = "售价格式不正确")
    private BigDecimal salePrice;

    private Boolean preSale;

    private LocalDateTime preSaleReleaseTime;

    private LocalDate publishDate;

    @Size(max = 30, message = "版本不能超过30个字符")
    private String edition;

    @Min(value = 1, message = "页数必须大于0")
    private Integer pages;

    private String description;

    @Size(max = 500, message = "封面地址不能超过500个字符")
    private String coverUrl;

    private List<Long> authorIds;//非 null 时整体替换作者列表，不能为空列表

    private List<Long> categoryIds;//非 null 时整体替换分类列表，允许空列表表示清空
}
