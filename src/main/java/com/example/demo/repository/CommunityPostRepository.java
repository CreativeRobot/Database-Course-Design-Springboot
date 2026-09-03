package com.example.demo.repository;

import com.example.demo.entity.CommunityPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    @Query("""
            select distinct post from CommunityPost post
            left join post.bookLinks link
            where post.status = 1
              and (:keyword is null or lower(post.title) like lower(concat('%', :keyword, '%')))
              and (:bookId is null or link.book.id = :bookId)
            order by post.createTime desc, post.id desc
            """)
    Page<CommunityPost> search(
            @Param("keyword") String keyword,
            @Param("bookId") Long bookId,
            Pageable pageable);

    @Query("select post from CommunityPost post where post.id = :id and post.status = 1")
    Optional<CommunityPost> findVisibleById(@Param("id") Long id);
}
