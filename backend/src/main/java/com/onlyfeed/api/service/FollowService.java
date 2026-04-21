package com.onlyfeed.api.service;

import com.onlyfeed.api.entity.Follow;
import com.onlyfeed.api.entity.UserAccount;
import com.onlyfeed.api.exception.ApiException;
import com.onlyfeed.api.repository.FollowRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserService userService;

    public FollowService(FollowRepository followRepository, UserService userService) {
        this.followRepository = followRepository;
        this.userService = userService;
    }

    @Transactional
    public void follow(Long followerId, Long targetUserId) {
        if (followerId.equals(targetUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot follow yourself");
        }

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, targetUserId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Already following this user");
        }

        UserAccount follower = userService.requireUser(followerId);
        UserAccount following = userService.requireUser(targetUserId);

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        followRepository.save(follow);
    }

    @Transactional
    public void unfollow(Long followerId, Long targetUserId) {
        followRepository.deleteByFollowerIdAndFollowingId(followerId, targetUserId);
    }
}
