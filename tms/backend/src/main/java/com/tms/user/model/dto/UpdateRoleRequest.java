package com.tms.user.model.dto;

import com.tms.common.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(@NotNull UserRole role) {
}
