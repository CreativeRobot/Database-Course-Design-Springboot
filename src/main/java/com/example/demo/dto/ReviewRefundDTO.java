package com.example.demo.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class ReviewRefundDTO {
    @NotNull private Boolean approved;
    private String remark;
}
