package com.tms.user.model.dto;

import com.tms.common.enums.UserRole;

public record UserResponse(Long id, String username, String displayName, String email, UserRole role) {
}
