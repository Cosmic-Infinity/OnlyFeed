package com.onlyfeed.api.dto.admin;

import java.time.Instant;

public record AdminPostResponse(
        Long id,
        String content,
        Instant createdAt,
        Long authorId,
        String authorUsername,
        long likeCount,
        long commentCount) {
}