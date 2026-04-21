package com.onlyfeed.api.repository;

import com.onlyfeed.api.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    long countByFollowingId(Long followingId);

    long countByFollowerId(Long followerId);

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    void deleteByFollowerId(Long followerId);

    void deleteByFollowingId(Long followingId);
}
