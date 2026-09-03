package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * BookCategoryId 数据库实体，映射业务领域中的持久化数据。
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCategoryId implements Serializable {

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "category_id")
    private Long categoryId;
}
