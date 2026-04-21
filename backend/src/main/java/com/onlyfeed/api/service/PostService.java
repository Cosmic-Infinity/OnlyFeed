package com.onlyfeed.api.service;

import com.onlyfeed.api.dto.post.CreatePostRequest;
import com.onlyfeed.api.dto.post.PostResponse;
import com.onlyfeed.api.dto.user.UserSummaryResponse;
import com.onlyfeed.api.entity.Post;
import com.onlyfeed.api.entity.PostLike;
import com.onlyfeed.api.entity.UserAccount;
import com.onlyfeed.api.exception.ApiException;
import com.onlyfeed.api.repository.CommentRepository;
import com.onlyfeed.api.repository.PostLikeRepository;
import com.onlyfeed.api.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;

    public PostService(
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            CommentRepository commentRepository,
            UserService userService) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> globalFeed(int page, int size, Long viewerId) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable(page, size))
                .map(post -> toResponse(post, viewerId));
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> followingFeed(Long userId, int page, int size) {
        return postRepository.findFollowingFeed(userId, pageable(page, size))
                .map(post -> toResponse(post, userId));
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> postsByUser(Long profileUserId, int page, int size, Long viewerId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(profileUserId, pageable(page, size))
                .map(post -> toResponse(post, viewerId));
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> myLikedPosts(Long userId, int page, int size) {
        return postRepository.findLikedPosts(userId, pageable(page, size))
                .map(post -> toResponse(post, userId));
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> myCommentedPosts(Long userId, int page, int size) {
        return postRepository.findCommentedPosts(userId, pageable(page, size))
                .map(post -> toResponse(post, userId));
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId, Long viewerId) {
        return toResponse(requirePost(postId), viewerId);
    }

    @Transactional
    public PostResponse createPost(Long authorId, CreatePostRequest request) {
        UserAccount author = userService.requireUser(authorId);
        Post post = new Post();
        post.setAuthor(author);
        post.setContent(request.content().trim());

        Post saved = postRepository.save(post);
        return toResponse(saved, authorId);
    }

    @Transactional
    public void deletePost(Long postId, Long requesterId) {
        Post post = requirePost(postId);
        if (!post.getAuthor().getId().equals(requesterId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only delete your own posts");
        }
        post.setContent("[This post was deleted]");
        postRepository.save(post);
    }

    @Transactional
    public void likePost(Long postId, Long userId) {
        if (postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Post already liked");
        }

        Post post = requirePost(postId);
        UserAccount user = userService.requireUser(userId);

        PostLike like = new PostLike();
        like.setPost(post);
        like.setUser(user);
        postLikeRepository.save(like);
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
        postLikeRepository.deleteByUserIdAndPostId(userId, postId);
    }

    public Post requirePost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    private PostResponse toResponse(Post post, Long viewerId) {
        long likes = postLikeRepository.countByPostId(post.getId());
        long comments = commentRepository.countByPostId(post.getId());
        boolean likedByMe = viewerId != null && postLikeRepository.existsByUserIdAndPostId(viewerId, post.getId());

        return new PostResponse(
                post.getId(),
                post.getContent(),
                post.getCreatedAt(),
                new UserSummaryResponse(post.getAuthor().getId(), post.getAuthor().getUsername()),
                likes,
                comments,
                likedByMe);
    }

    private Pageable pageable(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(50, Math.max(1, size));
        return PageRequest.of(safePage, safeSize);
    }
}
