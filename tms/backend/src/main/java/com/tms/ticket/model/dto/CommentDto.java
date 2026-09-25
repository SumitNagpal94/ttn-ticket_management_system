package com.tms.ticket.model.dto;

import com.tms.common.dto.UserSummaryDto;

import java.time.Instant;

public record CommentDto(Long id, String body, UserSummaryDto author, Instant createdAt) {
}
