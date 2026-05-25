package com.taskmanagement.dto;

import com.taskmanagement.entity.Project;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        UserResponse owner,
        Set<UserResponse> members,
        Set<UserResponse> admins
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt(),
                UserResponse.from(project.getOwner()),
                project.getMembers().stream().map(UserResponse::from).collect(Collectors.toSet()),
                project.getAdmins().stream().map(UserResponse::from).collect(Collectors.toSet())
        );
    }
}
