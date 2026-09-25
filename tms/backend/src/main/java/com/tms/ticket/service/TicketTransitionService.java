package com.tms.ticket.service;

import com.tms.common.enums.ErrorCode;
import com.tms.common.enums.TicketStatus;
import com.tms.common.exception.TmsException;
import com.tms.common.security.AuthenticatedUser;
import com.tms.common.util.SecurityUtil;
import com.tms.ticket.model.dto.TicketDetailDto;
import com.tms.ticket.model.entity.Ticket;
import com.tms.ticket.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketTransitionService {

    private static final Logger log = LoggerFactory.getLogger(TicketTransitionService.class);

    private final TicketRepository ticketRepository;
    private final TicketTransitionPolicy policy;
    private final TicketMapper ticketMapper;

    public TicketTransitionService(
            TicketRepository ticketRepository,
            TicketTransitionPolicy policy,
            TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.policy = policy;
        this.ticketMapper = ticketMapper;
    }

    @Transactional
    public TicketDetailDto transition(Long ticketId, TicketStatus targetStatus) {
        AuthenticatedUser actor = SecurityUtil.currentUser();
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TmsException(ErrorCode.NOT_FOUND, "Ticket not found"));
        TicketStatus from = ticket.getStatus();
        policy.validate(ticket, targetStatus, actor);
        ticket.setStatus(targetStatus);
        Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket {} transitioned {} -> {} by {}", ticketId, from, targetStatus, actor.username());
        return ticketMapper.toDetail(saved);
    }
}
