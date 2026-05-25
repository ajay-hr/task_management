package com.taskmanagement.controller;

import com.taskmanagement.dto.PasswordResetRequest;
import com.taskmanagement.dto.UserResponse;
import com.taskmanagement.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUserService adminUserService;

    @PostMapping("/users/{email}/reset-password")
    public UserResponse resetPassword(@PathVariable String email, @Valid @RequestBody PasswordResetRequest request) {
        return adminUserService.resetPassword(email, request);
    }
}
