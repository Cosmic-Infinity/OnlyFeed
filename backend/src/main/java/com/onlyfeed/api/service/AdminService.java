package com.onlyfeed.api.service;

import com.onlyfeed.api.dto.admin.AdminCommentResponse;
import com.onlyfeed.api.dto.admin.AdminCreateUserRequest;
import com.onlyfeed.api.dto.admin.AdminPostResponse;
import com.onlyfeed.api.dto.admin.AdminUserResponse;
import com.onlyfeed.api.entity.Comment;
import com.onlyfeed.api.entity.Post;
import com.onlyfeed.api.entity.UserAccount;
import com.onlyfeed.api.entity.UserRole;
import com.onlyfeed.api.exception.ApiException;
import com.onlyfeed.api.repository.CommentRepository;
import com.onlyfeed.api.repository.FollowRepository;
import com.onlyfeed.api.repository.PostLikeRepository;
import com.onlyfeed.api.repository.PostRepository;
import com.onlyfeed.api.repository.UserRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            UserRepository userRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            PostLikeRepository postLikeRepository,
            FollowRepository followRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.postLikeRepository = postLikeRepository;
        this.followRepository = followRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> listUsers(int page, int size) {
        return userRepository.findAllByOrderByCreatedAtDesc(pageable(page, size))
                .map(user -> new AdminUserResponse(user.getId(), user.getUsername(), effectiveRole(user),
                        user.getCreatedAt()));
    }

    @Transactional
    public AdminUserResponse createUser(AdminCreateUserRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "Username already taken");
        }

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setRole(UserRole.USER);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        UserAccount saved = userRepository.save(user);
        return new AdminUserResponse(saved.getId(), saved.getUsername(), effectiveRole(saved), saved.getCreatedAt());
    }

    @Transactional
    public void deleteUser(Long userId, Long requesterId) {
        if (userId.equals(requesterId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot delete your own account from admin panel");
        }

        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        List<Post> posts = postRepository.findByAuthorIdOrderByCreatedAtDesc(userId, Pageable.unpaged()).getContent();
        for (Post post : posts) {
            deletePostInternal(post.getId());
        }

        commentRepository.deleteByAuthorId(userId);
        postLikeRepository.deleteByUserId(userId);
        followRepository.deleteByFollowerId(userId);
        followRepository.deleteByFollowingId(userId);
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public Page<AdminPostResponse> listPosts(int page, int size) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable(page, size))
                .map(this::toAdminPostResponse);
    }

    @Transactional
    public void deletePost(Long postId) {
        deletePostInternal(postId);
    }

    @Transactional(readOnly = true)
    public Page<AdminCommentResponse> listComments(int page, int size) {
        return commentRepository.findAllByOrderByCreatedAtDesc(pageable(page, size))
                .map(comment -> new AdminCommentResponse(
                        comment.getId(),
                        comment.getContent(),
                        comment.getCreatedAt(),
                        comment.getAuthor().getId(),
                        comment.getAuthor().getUsername(),
                        comment.getPost().getId()));
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Comment not found"));
        commentRepository.delete(comment);
    }

    private void deletePostInternal(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found"));
        commentRepository.deleteByPostId(postId);
        postLikeRepository.deleteByPostId(postId);
        postRepository.delete(post);
    }

    private AdminPostResponse toAdminPostResponse(Post post) {
        return new AdminPostResponse(
                post.getId(),
                post.getContent(),
                post.getCreatedAt(),
                post.getAuthor().getId(),
                post.getAuthor().getUsername(),
                postLikeRepository.countByPostId(post.getId()),
                commentRepository.countByPostId(post.getId()));
    }

    private String effectiveRole(UserAccount user) {
        return (user.getRole() == null ? UserRole.USER : user.getRole()).name();
    }

    private Pageable pageable(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(50, Math.max(1, size));
        return PageRequest.of(safePage, safeSize);
    }
}