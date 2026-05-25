package com.taskmanagement.dto;

import com.taskmanagement.entity.User;

public record UserResponse(
        Long id,
        String userCode,
        String name,
        String email,
        String role
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUserCode(), user.getName(), user.getEmail(), user.getRole());
    }
}
