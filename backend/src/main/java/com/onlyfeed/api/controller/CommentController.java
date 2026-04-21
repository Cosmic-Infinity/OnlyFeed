package com.onlyfeed.api.controller;

import com.onlyfeed.api.dto.ApiMessage;
import com.onlyfeed.api.dto.comment.CommentResponse;
import com.onlyfeed.api.dto.comment.CreateCommentRequest;
import com.onlyfeed.api.security.AuthContext;
import com.onlyfeed.api.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/posts/{postId}/comments")
    public Page<CommentResponse> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return commentService.getPostComments(postId, page, size);
    }

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(@PathVariable Long postId, @Valid @RequestBody CreateCommentRequest request) {
        return commentService.createComment(postId, currentUserId(), request);
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiMessage deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId, currentUserId());
        return new ApiMessage("Comment deleted");
    }

    private Long currentUserId() {
        return AuthContext.requireCurrentUserId();
    }
}
