package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 手动库存调整请求体。
 * changeQuantity 为正表示入库，为负表示出库。
 */
@Data
public class StockAdjustDTO {

    @NotNull(message = "变动数量不能为空")
    private Integer changeQuantity;

    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
