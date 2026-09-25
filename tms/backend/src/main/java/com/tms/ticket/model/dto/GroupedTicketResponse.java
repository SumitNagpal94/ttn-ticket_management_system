package com.tms.ticket.model.dto;

import com.tms.common.enums.TicketStatus;

import java.util.List;

public record GroupedTicketResponse(int page, int size, List<StatusSectionDto> sections) {

    public record StatusSectionDto(
            TicketStatus status,
            List<TicketSummaryDto> tickets,
            long totalElements,
            int totalPages) {
    }
}
