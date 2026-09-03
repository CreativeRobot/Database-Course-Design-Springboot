package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * UpdateCartSelectionDTO 请求数据传输对象，用于接收和校验接口输入参数。
 */
@Data
public class UpdateCartSelectionDTO {

    @NotNull(message = "选中状态不能为空")
    private Boolean selected;
}
