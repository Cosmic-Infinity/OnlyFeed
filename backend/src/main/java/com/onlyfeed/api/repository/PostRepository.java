package com.onlyfeed.api.repository;

import com.onlyfeed.api.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = "author")
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = "author")
    Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    @Query(value = """
            select p from Post p
            join Follow f on p.author.id = f.following.id
            where f.follower.id = :userId
            order by p.createdAt desc
            """, countQuery = """
            select count(p) from Post p
            join Follow f on p.author.id = f.following.id
            where f.follower.id = :userId
            """)
    Page<Post> findFollowingFeed(@Param("userId") Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    @Query(value = """
            select pl.post from PostLike pl
            where pl.user.id = :userId
            order by pl.createdAt desc
            """, countQuery = """
            select count(pl) from PostLike pl where pl.user.id = :userId
            """)
    Page<Post> findLikedPosts(@Param("userId") Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    @Query(value = """
            select distinct c.post from Comment c
            where c.author.id = :userId
            order by c.post.createdAt desc
            """, countQuery = """
            select count(distinct c.post.id) from Comment c where c.author.id = :userId
            """)
    Page<Post> findCommentedPosts(@Param("userId") Long userId, Pageable pageable);
}
