package com.onlyfeed.api.controller;

import com.onlyfeed.api.dto.ApiMessage;
import com.onlyfeed.api.dto.post.CreatePostRequest;
import com.onlyfeed.api.dto.post.PostResponse;
import com.onlyfeed.api.security.AuthContext;
import com.onlyfeed.api.service.PostService;
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
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/global")
    public Page<PostResponse> globalFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long viewerId = AuthContext.currentPrincipal().map(principal -> principal.getId()).orElse(null);
        return postService.globalFeed(page, size, viewerId);
    }

    @GetMapping("/following")
    public Page<PostResponse> followingFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return postService.followingFeed(currentUserId(), page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPost(@Valid @RequestBody CreatePostRequest request) {
        return postService.createPost(currentUserId(), request);
    }

    @GetMapping("/{postId}")
    public PostResponse getPost(@PathVariable Long postId) {
        Long viewerId = AuthContext.currentPrincipal().map(principal -> principal.getId()).orElse(null);
        return postService.getPost(postId, viewerId);
    }

    @DeleteMapping("/{postId}")
    public ApiMessage deletePost(@PathVariable Long postId) {
        postService.deletePost(postId, currentUserId());
        return new ApiMessage("Post deleted");
    }

    @PostMapping("/{postId}/likes")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiMessage likePost(@PathVariable Long postId) {
        postService.likePost(postId, currentUserId());
        return new ApiMessage("Post liked");
    }

    @DeleteMapping("/{postId}/likes")
    public ApiMessage unlikePost(@PathVariable Long postId) {
        postService.unlikePost(postId, currentUserId());
        return new ApiMessage("Post unliked");
    }

    private Long currentUserId() {
        return AuthContext.requireCurrentUserId();
    }
}
