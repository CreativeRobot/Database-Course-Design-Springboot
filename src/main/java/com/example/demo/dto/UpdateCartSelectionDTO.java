package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartSelectionDTO {

    @NotNull(message = "选中状态不能为空")
    private Boolean selected;
}
