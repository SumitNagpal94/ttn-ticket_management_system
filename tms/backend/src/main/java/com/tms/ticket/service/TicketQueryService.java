package com.tms.ticket.service;

import com.tms.common.config.TmsConfig;
import com.tms.common.enums.ErrorCode;
import com.tms.common.enums.TicketStatus;
import com.tms.common.exception.TmsException;
import com.tms.common.security.AuthenticatedUser;
import com.tms.common.util.SecurityUtil;
import com.tms.ticket.model.dto.GroupedTicketResponse;
import com.tms.ticket.model.dto.TicketDetailDto;
import com.tms.ticket.model.dto.TicketSummaryDto;
import com.tms.ticket.model.entity.Ticket;
import com.tms.ticket.repository.TicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class TicketQueryService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final TmsConfig tmsConfig;

    public TicketQueryService(TicketRepository ticketRepository, TicketMapper ticketMapper, TmsConfig tmsConfig) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
        this.tmsConfig = tmsConfig;
    }

    @Transactional(readOnly = true)
    public TicketDetailDto findById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TmsException(ErrorCode.NOT_FOUND, "Ticket not found"));
        return ticketMapper.toDetail(ticket);
    }

    @Transactional(readOnly = true)
    public GroupedTicketResponse findGrouped(Long assigneeId, String keyword, int page, Integer size) {
        AuthenticatedUser actor = SecurityUtil.currentUser();
        if (actor == null) {
            throw new TmsException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        int pageSize = size != null ? size : tmsConfig.getPagination().getDefaultPageSize();
        String q = normalizeKeyword(keyword);

        List<GroupedTicketResponse.StatusSectionDto> sections = Arrays.stream(TicketStatus.values())
                .map(status -> toSection(status, assigneeId, q, page, pageSize))
                .toList();
        return new GroupedTicketResponse(page, pageSize, sections);
    }

    private GroupedTicketResponse.StatusSectionDto toSection(
            TicketStatus status, Long assigneeId, String keyword, int page, int pageSize) {
        Page<Ticket> result = ticketRepository.findByFilters(
                status, assigneeId, keyword, PageRequest.of(page, pageSize));
        List<TicketSummaryDto> tickets = result.getContent().stream()
                .map(ticketMapper::toSummary)
                .toList();
        return new GroupedTicketResponse.StatusSectionDto(
                status, tickets, result.getTotalElements(), result.getTotalPages());
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "";
        }
        return keyword.trim();
    }
}
