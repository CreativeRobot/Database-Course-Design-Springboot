package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateSecurityQuestionsDTO {
    @Valid
    @Size(min = 3, max = 3, message = "请设置三个密保问题")
    private List<SecurityQuestionAnswerDTO> questions;
}
