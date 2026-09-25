package com.tms.ticket.service;

import com.tms.common.enums.ErrorCode;
import com.tms.common.enums.TicketStatus;
import com.tms.common.enums.UserRole;
import com.tms.common.exception.TmsException;
import com.tms.common.security.AuthenticatedUser;
import com.tms.ticket.model.entity.Ticket;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class TicketTransitionPolicy {

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED = Map.of(
            TicketStatus.OPEN, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED),
            TicketStatus.IN_PROGRESS, Set.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED),
            TicketStatus.RESOLVED, Set.of(TicketStatus.CLOSED),
            TicketStatus.CLOSED, Set.of(),
            TicketStatus.CANCELLED, Set.of());

    public void validate(Ticket ticket, TicketStatus target, AuthenticatedUser actor) {
        if (actor == null) {
            throw new TmsException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        TicketStatus current = ticket.getStatus();
        Set<TicketStatus> allowedTargets = ALLOWED.getOrDefault(current, Set.of());
        if (!allowedTargets.contains(target)) {
            throw new TmsException(ErrorCode.INVALID_TRANSITION,
                    "Cannot transition from " + current + " to " + target);
        }
        if (target == TicketStatus.CLOSED) {
            boolean isCreator = actor.userId() != null
                    && ticket.getCreatedBy().getId().equals(actor.userId());
            boolean isQa = actor.role() == UserRole.QA;
            if (!isCreator && !isQa) {
                throw new TmsException(ErrorCode.FORBIDDEN,
                        "Only ticket creator or QA can close a resolved ticket");
            }
            return;
        }
        if (actor.role() != UserRole.DEVELOPER && actor.role() != UserRole.QA) {
            throw new TmsException(ErrorCode.FORBIDDEN, "Only Developer or QA can transition ticket status");
        }
    }
}
