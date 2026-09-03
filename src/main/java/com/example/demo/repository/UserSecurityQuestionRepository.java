package com.example.demo.repository;

import com.example.demo.entity.UserSecurityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSecurityQuestionRepository extends JpaRepository<UserSecurityQuestion, Long> {
    List<UserSecurityQuestion> findByUser_IdOrderByIdAsc(Long userId);
    long countByUser_Id(Long userId);
    void deleteByUser_Id(Long userId);
}
