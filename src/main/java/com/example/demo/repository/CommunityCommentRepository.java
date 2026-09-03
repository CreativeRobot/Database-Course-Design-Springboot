package com.example.demo.repository;

import com.example.demo.entity.CommunityComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    Page<CommunityComment> findByPost_IdAndStatus(Long postId, Integer status, Pageable pageable);

    @Query("select comment from CommunityComment comment where comment.id = :id and comment.post.id = :postId and comment.status = 1")
    Optional<CommunityComment> findVisibleByIdAndPostId(
            @Param("id") Long id, @Param("postId") Long postId);

    long countByPost_IdAndStatus(Long postId, Integer status);
}
