package com.tms.user.model.dto;

import com.tms.common.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String displayName,
        @NotBlank @Email String email,
        @NotNull UserRole role) {
}
