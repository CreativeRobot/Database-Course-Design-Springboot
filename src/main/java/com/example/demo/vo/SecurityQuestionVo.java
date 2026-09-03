package com.example.demo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SecurityQuestionVo {
    private String key;
    private String question;
}
