package com.onlyfeed.api.repository;

import com.onlyfeed.api.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);

    Page<Comment> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByPostId(Long postId);

    boolean existsByIdAndAuthorId(Long id, Long authorId);

    void deleteByAuthorId(Long authorId);

    void deleteByPostId(Long postId);
}
