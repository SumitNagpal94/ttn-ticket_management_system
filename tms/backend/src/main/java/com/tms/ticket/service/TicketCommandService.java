package com.tms.ticket.service;

import com.tms.common.enums.ErrorCode;
import com.tms.common.enums.TicketPriority;
import com.tms.common.enums.TicketStatus;
import com.tms.common.exception.TmsException;
import com.tms.common.security.AuthenticatedUser;
import com.tms.common.util.SecurityUtil;
import com.tms.ticket.model.dto.CreateTicketRequest;
import com.tms.ticket.model.dto.TicketDetailDto;
import com.tms.ticket.model.dto.UpdateTicketRequest;
import com.tms.ticket.model.entity.Ticket;
import com.tms.ticket.repository.TicketRepository;
import com.tms.user.model.entity.User;
import com.tms.user.repository.UserRepository;
import com.tms.user.security.DatabaseUserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketCommandService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final DatabaseUserDetailsService userDetailsService;
    private final TicketMapper ticketMapper;

    public TicketCommandService(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            DatabaseUserDetailsService userDetailsService,
            TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        this.ticketMapper = ticketMapper;
    }

    @Transactional
    public TicketDetailDto create(CreateTicketRequest request) {
        AuthenticatedUser actor = requireActor();
        User creator = resolveDbUser(actor);
        Ticket ticket = new Ticket();
        ticket.setTitle(trimRequired(request.title(), "title"));
        ticket.setDescription(trimRequired(request.description(), "description"));
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(request.priority() != null ? request.priority() : TicketPriority.MEDIUM);
        ticket.setCreatedBy(creator);
        ticket.setAssignee(resolveAssignee(request.assigneeId()));
        return ticketMapper.toDetail(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDetailDto update(Long ticketId, UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TmsException(ErrorCode.NOT_FOUND, "Ticket not found"));
        if (request.title() != null) {
            ticket.setTitle(trimRequired(request.title(), "title"));
        }
        if (request.description() != null) {
            ticket.setDescription(trimRequired(request.description(), "description"));
        }
        if (request.priority() != null) {
            ticket.setPriority(request.priority());
        }
        if (Boolean.TRUE.equals(request.clearAssignee())) {
            ticket.setAssignee(null);
        } else if (request.assigneeId() != null) {
            ticket.setAssignee(resolveAssignee(request.assigneeId()));
        }
        return ticketMapper.toDetail(ticketRepository.save(ticket));
    }

    private User resolveAssignee(Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return userRepository.findById(assigneeId)
                .orElseThrow(() -> new TmsException(ErrorCode.VALIDATION_ERROR, "Assignee not found"));
    }

    private User resolveDbUser(AuthenticatedUser actor) {
        if (actor.userId() != null) {
            return userRepository.findById(actor.userId())
                    .orElseThrow(() -> new TmsException(ErrorCode.NOT_FOUND, "User not found"));
        }
        return userDetailsService.loadEntity(actor.username());
    }

    private AuthenticatedUser requireActor() {
        AuthenticatedUser actor = SecurityUtil.currentUser();
        if (actor == null) {
            throw new TmsException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        return actor;
    }

    private String trimRequired(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new TmsException(ErrorCode.VALIDATION_ERROR, field + " is required");
        }
        return value.trim();
    }
}
