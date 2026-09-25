package com.tms.ticket.model.dto;

import com.tms.common.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String description,
        TicketPriority priority,
        Long assigneeId) {
}
