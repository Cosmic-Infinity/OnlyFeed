package com.onlyfeed.api.dto.user;

import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String username,
        Instant createdAt,
        long followerCount,
        long followingCount,
        boolean followedByMe) {
}
