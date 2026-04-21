package com.onlyfeed.api.dto.admin;

import java.time.Instant;

public record AdminCommentResponse(
        Long id,
        String content,
        Instant createdAt,
        Long authorId,
        String authorUsername,
        Long postId) {
}