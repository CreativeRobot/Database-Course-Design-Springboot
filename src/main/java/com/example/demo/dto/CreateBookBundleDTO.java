package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateBookBundleDTO {
    @NotBlank(message = "组合包名称不能为空")
    @Size(max = 100, message = "组合包名称不能超过100个字符")
    private String name;

    @Size(max = 500, message = "组合包说明不能超过500个字符")
    private String description;

    @NotNull(message = "组合包价格不能为空")
    @DecimalMin(value = "0.00", message = "组合包价格不能为负数")
    private BigDecimal bundlePrice;

    @NotNull(message = "组合包图书不能为空")
    private List<Long> bookIds;
}
