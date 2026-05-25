package com.taskmanagement.dto;

import com.taskmanagement.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.Set;

public record TaskRequest(
        @NotBlank(message = "Task title is required") String title,
        String description,
        TaskStatus status,
        LocalDateTime dueDate,
        Set<Long> assignedToIds,
        Set<String> assignedToUserCodes,
        Set<String> assignedToEmails
) {
}
