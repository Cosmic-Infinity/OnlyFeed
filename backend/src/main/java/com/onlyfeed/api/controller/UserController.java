package com.onlyfeed.api.controller;

import com.onlyfeed.api.dto.ApiMessage;
import com.onlyfeed.api.dto.post.PostResponse;
import com.onlyfeed.api.dto.user.UserProfileResponse;
import com.onlyfeed.api.dto.user.UserSummaryResponse;
import com.onlyfeed.api.security.AuthContext;
import com.onlyfeed.api.service.FollowService;
import com.onlyfeed.api.service.PostService;
import com.onlyfeed.api.service.UserService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FollowService followService;
    private final PostService postService;

    public UserController(UserService userService, FollowService followService, PostService postService) {
        this.userService = userService;
        this.followService = followService;
        this.postService = postService;
    }

    @GetMapping("/profile/{username}")
    public UserProfileResponse profile(@PathVariable String username) {
        Long viewerId = AuthContext.currentPrincipal().map(principal -> principal.getId()).orElse(null);
        return userService.getProfileByUsername(username, viewerId);
    }

    @GetMapping("/{userId}/posts")
    public Page<PostResponse> postsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long viewerId = AuthContext.currentPrincipal().map(principal -> principal.getId()).orElse(null);
        return postService.postsByUser(userId, page, size, viewerId);
    }

    @GetMapping("/search")
    public List<UserSummaryResponse> search(@RequestParam String query) {
        return userService.search(query);
    }

    @PostMapping("/{userId}/follow")
    public ApiMessage follow(@PathVariable Long userId) {
        followService.follow(currentUserId(), userId);
        return new ApiMessage("User followed");
    }

    @DeleteMapping("/{userId}/follow")
    public ApiMessage unfollow(@PathVariable Long userId) {
        followService.unfollow(currentUserId(), userId);
        return new ApiMessage("User unfollowed");
    }

    @GetMapping("/me/posts")
    public Page<PostResponse> myPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long me = currentUserId();
        return postService.postsByUser(me, page, size, me);
    }

    @GetMapping("/me/liked-posts")
    public Page<PostResponse> myLikedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return postService.myLikedPosts(currentUserId(), page, size);
    }

    @GetMapping("/me/commented-posts")
    public Page<PostResponse> myCommentedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return postService.myCommentedPosts(currentUserId(), page, size);
    }

    private Long currentUserId() {
        return AuthContext.requireCurrentUserId();
    }
}
