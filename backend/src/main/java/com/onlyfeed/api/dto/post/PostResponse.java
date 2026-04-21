package com.onlyfeed.api.dto.post;

import com.onlyfeed.api.dto.user.UserSummaryResponse;
import java.time.Instant;

public record PostResponse(
        Long id,
        String content,
        Instant createdAt,
        UserSummaryResponse author,
        long likeCount,
        long commentCount,
        boolean likedByMe) {
}
