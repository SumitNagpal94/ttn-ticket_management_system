package com.tms.ticket.model.dto;

import com.tms.common.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record TransitionRequest(@NotNull TicketStatus targetStatus) {
}
