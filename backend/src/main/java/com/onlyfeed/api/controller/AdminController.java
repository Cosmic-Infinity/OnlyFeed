package com.onlyfeed.api.controller;

import com.onlyfeed.api.dto.ApiMessage;
import com.onlyfeed.api.dto.admin.AdminCommentResponse;
import com.onlyfeed.api.dto.admin.AdminCreateUserRequest;
import com.onlyfeed.api.dto.admin.AdminPostResponse;
import com.onlyfeed.api.dto.admin.AdminUserResponse;
import com.onlyfeed.api.security.AuthContext;
import com.onlyfeed.api.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public Page<AdminUserResponse> users(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminService.listUsers(page, size);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponse createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        return adminService.createUser(request);
    }

    @DeleteMapping("/users/{userId}")
    public ApiMessage deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId, AuthContext.requireCurrentUserId());
        return new ApiMessage("User deleted");
    }

    @GetMapping("/posts")
    public Page<AdminPostResponse> posts(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminService.listPosts(page, size);
    }

    @DeleteMapping("/posts/{postId}")
    public ApiMessage deletePost(@PathVariable Long postId) {
        adminService.deletePost(postId);
        return new ApiMessage("Post deleted");
    }

    @GetMapping("/comments")
    public Page<AdminCommentResponse> comments(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminService.listComments(page, size);
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiMessage deleteComment(@PathVariable Long commentId) {
        adminService.deleteComment(commentId);
        return new ApiMessage("Comment deleted");
    }
}