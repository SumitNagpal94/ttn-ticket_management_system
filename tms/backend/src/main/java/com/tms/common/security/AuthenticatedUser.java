package com.tms.common.security;

import com.tms.common.enums.UserRole;

public record AuthenticatedUser(Long userId, String username, UserRole role) {
}
