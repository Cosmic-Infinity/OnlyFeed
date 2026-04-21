package com.onlyfeed.api.service;

import com.onlyfeed.api.dto.user.UserProfileResponse;
import com.onlyfeed.api.dto.user.UserSummaryResponse;
import com.onlyfeed.api.entity.UserAccount;
import com.onlyfeed.api.exception.ApiException;
import com.onlyfeed.api.repository.FollowRepository;
import com.onlyfeed.api.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public UserService(UserRepository userRepository, FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    public UserAccount requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public UserAccount requireUserByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public UserProfileResponse getProfileByUsername(String username, Long viewerId) {
        UserAccount user = requireUserByUsername(username);
        long followers = followRepository.countByFollowingId(user.getId());
        long following = followRepository.countByFollowerId(user.getId());
        boolean followedByMe = viewerId != null
                && followRepository.existsByFollowerIdAndFollowingId(viewerId, user.getId());

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getCreatedAt(),
                followers,
                following,
                followedByMe);
    }

    public List<UserSummaryResponse> search(String query) {
        String normalized = query == null ? "" : query.trim();
        if (normalized.isBlank()) {
            return List.of();
        }

        return userRepository.findTop10ByUsernameContainingIgnoreCaseOrderByUsernameAsc(normalized)
                .stream()
                .map(user -> new UserSummaryResponse(user.getId(), user.getUsername()))
                .toList();
    }
}
