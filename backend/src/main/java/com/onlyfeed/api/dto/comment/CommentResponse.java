package com.onlyfeed.api.dto.comment;

import com.onlyfeed.api.dto.user.UserSummaryResponse;
import java.time.Instant;

public record CommentResponse(
        Long id,
        String content,
        Instant createdAt,
        UserSummaryResponse author,
        Long postId) {
}
