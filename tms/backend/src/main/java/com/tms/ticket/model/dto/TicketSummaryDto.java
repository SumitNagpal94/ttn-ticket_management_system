package com.tms.ticket.model.dto;

import com.tms.common.dto.UserSummaryDto;
import com.tms.common.enums.TicketPriority;
import com.tms.common.enums.TicketStatus;

import java.time.Instant;

public record TicketSummaryDto(
        Long id,
        String title,
        TicketStatus status,
        TicketPriority priority,
        UserSummaryDto assignee,
        UserSummaryDto createdBy,
        Instant createdAt,
        Instant updatedAt) {
}
