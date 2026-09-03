package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_security_question", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "question_key"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSecurityQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "question_key", nullable = false, length = 30)
    private String questionKey;

    @Column(name = "answer_hash", nullable = false, length = 100)
    private String answerHash;
}
