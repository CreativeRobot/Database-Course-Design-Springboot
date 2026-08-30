package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookCreateDTO {

    @NotBlank(message = "ISBN不能为空")
    @Size(max = 20, message = "ISBN不能超过20个字符")
    private String isbn;

    @NotBlank(message = "书名不能为空")
    @Size(max = 200, message = "书名不能超过200个字符")
    private String title;

    @NotNull(message = "出版社不能为空")
    private Long publisherId;

    @NotNull(message = "原价不能为空")
    @DecimalMin(value = "0.00", message = "原价不能为负数")
    @Digits(integer = 8, fraction = 2, message = "原价格式不正确")
    private BigDecimal originalPrice;

    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.00", message = "售价不能为负数")
    @Digits(integer = 8, fraction = 2, message = "售价格式不正确")
    private BigDecimal salePrice;

    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

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

    @NotEmpty(message = "至少需要一位作者")
    private List<Long> authorIds;//按列表顺序作为作者署名顺序

    private List<Long> categoryIds;
}
