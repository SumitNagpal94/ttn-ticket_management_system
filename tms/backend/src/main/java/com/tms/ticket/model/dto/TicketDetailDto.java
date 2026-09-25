package com.tms.ticket.model.dto;

import com.tms.common.dto.UserSummaryDto;
import com.tms.common.enums.TicketPriority;
import com.tms.common.enums.TicketStatus;

import java.time.Instant;
import java.util.List;

public record TicketDetailDto(
        Long id,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        UserSummaryDto assignee,
        UserSummaryDto createdBy,
        Instant createdAt,
        Instant updatedAt,
        List<CommentDto> comments) {
}
