package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BookAuthor 数据库实体，映射业务领域中的持久化数据。
 */
@Entity
@Table(name = "book_author")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookAuthor {

    @EmbeddedId
    private BookAuthorId id;

    @MapsId("bookId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @MapsId("authorId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @Builder.Default
    @Column(nullable = false)
    private Integer authorOrder = 1;//作者顺序
}
