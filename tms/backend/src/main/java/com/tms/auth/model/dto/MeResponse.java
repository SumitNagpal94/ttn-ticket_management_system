package com.tms.auth.model.dto;

import com.tms.common.enums.UserRole;

public record MeResponse(Long id, String username, String displayName, String email, UserRole role) {
}
