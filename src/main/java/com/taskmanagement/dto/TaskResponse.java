package com.taskmanagement.dto;

import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.TaskStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        LocalDateTime dueDate,
        Set<UserResponse> assignees,
        Long projectId,
        String projectName
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                task.getAssignees().stream().map(UserResponse::from).collect(Collectors.toSet()),
                task.getProject().getId(),
                task.getProject().getName()
        );
    }
}
