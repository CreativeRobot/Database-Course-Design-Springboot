package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
