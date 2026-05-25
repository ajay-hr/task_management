package com.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record ProjectRequest(
        @NotBlank(message = "Project name is required") String name,
        String description,
        Set<Long> memberIds,
        Set<String> memberUserCodes,
        Set<String> memberEmails
) {
}
