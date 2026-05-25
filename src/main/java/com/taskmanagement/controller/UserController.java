package com.taskmanagement.controller;

import com.taskmanagement.dto.PasswordResetRequest;
import com.taskmanagement.dto.UserResponse;
import com.taskmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/reset-password")
    public UserResponse resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        return userService.resetMyPassword(request);
    }
}
