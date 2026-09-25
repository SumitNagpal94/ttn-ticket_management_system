package com.tms.ticket.service;

import com.tms.common.enums.ErrorCode;
import com.tms.common.enums.TicketStatus;
import com.tms.common.enums.UserRole;
import com.tms.common.exception.TmsException;
import com.tms.common.security.AuthenticatedUser;
import com.tms.ticket.model.entity.Ticket;
import com.tms.user.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketTransitionPolicyTest {

    private TicketTransitionPolicy policy;
    private Ticket ticket;
    private User creator;

    @BeforeEach
    void setUp() {
        policy = new TicketTransitionPolicy();
        ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);
        creator = new User();
        creator.setId(10L);
        ticket.setCreatedBy(creator);
    }

    @Test
    void validOpenToInProgress() {
        assertDoesNotThrow(() -> policy.validate(ticket, TicketStatus.IN_PROGRESS, dev()));
    }

    @Test
    void validInProgressToResolved() {
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        assertDoesNotThrow(() -> policy.validate(ticket, TicketStatus.RESOLVED, qa()));
    }

    @Test
    void validResolvedToClosedByCreator() {
        ticket.setStatus(TicketStatus.RESOLVED);
        assertDoesNotThrow(() -> policy.validate(ticket, TicketStatus.CLOSED, user(10L, UserRole.USER)));
    }

    @Test
    void validResolvedToClosedByQa() {
        ticket.setStatus(TicketStatus.RESOLVED);
        assertDoesNotThrow(() -> policy.validate(ticket, TicketStatus.CLOSED, user(99L, UserRole.QA)));
    }

    @Test
    void invalidClosedToOpen() {
        ticket.setStatus(TicketStatus.CLOSED);
        TmsException ex = assertThrows(TmsException.class,
                () -> policy.validate(ticket, TicketStatus.OPEN, dev()));
        assertEquals(ErrorCode.INVALID_TRANSITION, ex.getErrorCode());
    }

    @Test
    void invalidOpenToResolved() {
        TmsException ex = assertThrows(TmsException.class,
                () -> policy.validate(ticket, TicketStatus.RESOLVED, dev()));
        assertEquals(ErrorCode.INVALID_TRANSITION, ex.getErrorCode());
    }

    @Test
    void forbiddenUserRole() {
        TmsException ex = assertThrows(TmsException.class,
                () -> policy.validate(ticket, TicketStatus.IN_PROGRESS, user(1L, UserRole.USER)));
        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
    }

    @Test
    void forbiddenDeveloperCloseOthersTicket() {
        ticket.setStatus(TicketStatus.RESOLVED);
        TmsException ex = assertThrows(TmsException.class,
                () -> policy.validate(ticket, TicketStatus.CLOSED, user(5L, UserRole.DEVELOPER)));
        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
    }

    private AuthenticatedUser dev() {
        return user(1L, UserRole.DEVELOPER);
    }

    private AuthenticatedUser qa() {
        return user(2L, UserRole.QA);
    }

    private AuthenticatedUser user(Long id, UserRole role) {
        return new AuthenticatedUser(id, "user" + id, role);
    }
}
