/**
 * ReviewRefundDTO 请求数据传输对象，用于接收和校验接口输入参数。
 */
package com.example.demo.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class ReviewRefundDTO {
    @NotNull private Boolean approved;
    private String remark;
}
