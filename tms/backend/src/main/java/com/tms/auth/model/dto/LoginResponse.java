package com.tms.auth.model.dto;

import com.tms.common.enums.UserRole;

public record LoginResponse(String token, String username, String displayName, UserRole role) {
}
