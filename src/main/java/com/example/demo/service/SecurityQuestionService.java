package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.SecurityQuestionAnswerDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.UserSecurityQuestion;
import com.example.demo.repository.UserSecurityQuestionRepository;
import com.example.demo.vo.SecurityQuestionVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class SecurityQuestionService {
    private static final Map<String, String> CATALOG = Map.ofEntries(
            Map.entry("Q1", "你的第一所学校名称是什么？"),
            Map.entry("Q2", "你最喜欢的作家是谁？"),
            Map.entry("Q3", "你最喜欢的书名是什么？"),
            Map.entry("Q4", "你出生城市的名称是什么？"),
            Map.entry("Q5", "你童年最喜欢的食物是什么？"),
            Map.entry("Q6", "你的第一只宠物叫什么？"),
            Map.entry("Q7", "你最喜欢的电影是什么？"),
            Map.entry("Q8", "你小学班主任的姓氏是什么？"));

    @Autowired private UserSecurityQuestionRepository repository;
    @Autowired private PasswordEncoder passwordEncoder;

    public List<SecurityQuestionVo> catalog() {
        return CATALOG.entrySet().stream().map(entry -> new SecurityQuestionVo(entry.getKey(), entry.getValue())).toList();
    }

    public List<SecurityQuestionVo> questionsForUser(Long userId) {
        return repository.findByUser_IdOrderByIdAsc(userId).stream()
                .map(item -> new SecurityQuestionVo(item.getQuestionKey(), CATALOG.get(item.getQuestionKey())))
                .toList();
    }

    public void replace(Long userId, User user, List<SecurityQuestionAnswerDTO> questions) {
        validateAnswers(questions, 3);
        repository.deleteByUser_Id(userId);
        repository.saveAll(questions.stream().map(item -> UserSecurityQuestion.builder()
                .user(user).questionKey(item.getQuestionKey().trim().toUpperCase(Locale.ROOT))
                .answerHash(passwordEncoder.encode(normalizeAnswer(item.getAnswer())))
                .build()).toList());
    }

    public void verify(Long userId, List<SecurityQuestionAnswerDTO> answers) {
        validateAnswers(answers, 2);
        Map<String, UserSecurityQuestion> saved = new HashMap<>();
        for (UserSecurityQuestion item : repository.findByUser_IdOrderByIdAsc(userId)) saved.put(item.getQuestionKey(), item);
        if (saved.size() < 3) throw new BusinessException(HttpStatus.BAD_REQUEST, "该账户未设置完整密保问题，无法找回或修改密码");
        int correct = 0;
        for (SecurityQuestionAnswerDTO answer : answers) {
            UserSecurityQuestion item = saved.get(answer.getQuestionKey().trim().toUpperCase(Locale.ROOT));
            if (item != null && passwordEncoder.matches(normalizeAnswer(answer.getAnswer()), item.getAnswerHash())) correct++;
        }
        if (correct < 2) throw new BusinessException(HttpStatus.UNAUTHORIZED, "密保答案不正确");
    }

    public void validateAnswers(List<SecurityQuestionAnswerDTO> answers, int expectedSize) {
        if (answers == null || answers.size() != expectedSize) throw new BusinessException(HttpStatus.BAD_REQUEST, "密保问题数量不正确");
        Set<String> keys = new HashSet<>();
        for (SecurityQuestionAnswerDTO answer : answers) {
            if (answer == null || !StringUtils.hasText(answer.getQuestionKey()) || !StringUtils.hasText(answer.getAnswer())) throw new BusinessException(HttpStatus.BAD_REQUEST, "密保问题和答案不能为空");
            String key = answer.getQuestionKey().trim().toUpperCase(Locale.ROOT);
            if (!CATALOG.containsKey(key) || !keys.add(key)) throw new BusinessException(HttpStatus.BAD_REQUEST, "密保问题必须选择三个不同的问题");
            answer.setQuestionKey(key);
        }
    }

    private String normalizeAnswer(String value) { return value.trim().toLowerCase(Locale.ROOT); }
}
