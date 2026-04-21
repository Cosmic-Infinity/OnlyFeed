package com.onlyfeed.api.service;

import com.onlyfeed.api.dto.comment.CommentResponse;
import com.onlyfeed.api.dto.comment.CreateCommentRequest;
import com.onlyfeed.api.dto.user.UserSummaryResponse;
import com.onlyfeed.api.entity.Comment;
import com.onlyfeed.api.entity.Post;
import com.onlyfeed.api.entity.UserAccount;
import com.onlyfeed.api.exception.ApiException;
import com.onlyfeed.api.repository.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserService userService;

    public CommentService(CommentRepository commentRepository, PostService postService, UserService userService) {
        this.commentRepository = commentRepository;
        this.postService = postService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getPostComments(Long postId, int page, int size) {
        postService.requirePost(postId);
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable(page, size))
                .map(this::toResponse);
    }

    @Transactional
    public CommentResponse createComment(Long postId, Long authorId, CreateCommentRequest request) {
        Post post = postService.requirePost(postId);
        UserAccount author = userService.requireUser(authorId);

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent(request.content().trim());

        return toResponse(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId, Long requesterId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (!comment.getAuthor().getId().equals(requesterId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                new UserSummaryResponse(comment.getAuthor().getId(), comment.getAuthor().getUsername()),
                comment.getPost().getId());
    }

    private Pageable pageable(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(50, Math.max(1, size));
        return PageRequest.of(safePage, safeSize);
    }
}
