package com.tms.ticket.model.dto;

import com.tms.common.enums.TicketPriority;
import jakarta.validation.constraints.Size;

public record UpdateTicketRequest(
        @Size(max = 200) String title,
        String description,
        TicketPriority priority,
        Long assigneeId,
        Boolean clearAssignee) {
}
