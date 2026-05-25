package com.taskmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @Schema(example = "user@gmail.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "must be a valid email address"
        )
        String email,
        @Schema(example = "Password@123")
        @NotBlank(message = "Password is required") String password
) {
}
