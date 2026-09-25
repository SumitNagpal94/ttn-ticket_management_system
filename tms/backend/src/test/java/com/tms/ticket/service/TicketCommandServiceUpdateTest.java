package com.tms.ticket.service;

import com.tms.common.enums.ErrorCode;
import com.tms.common.enums.UserRole;
import com.tms.common.exception.TmsException;
import com.tms.support.IntegrationTestSupport;
import com.tms.ticket.model.dto.CreateTicketRequest;
import com.tms.ticket.model.dto.TicketDetailDto;
import com.tms.ticket.model.dto.UpdateTicketRequest;
import com.tms.user.model.entity.User;
import com.tms.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("dev")
class TicketCommandServiceUpdateTest {

    @Autowired
    private TicketCommandService commandService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User creator;
    private User assignee;
    private Long ticketId;

    @BeforeEach
    void setUp() {
        creator = saveUser("creator", UserRole.USER);
        assignee = saveUser("assignee", UserRole.DEVELOPER);
        IntegrationTestSupport.setSecurityContext(creator.getId(), creator.getUsername(), UserRole.USER);
        ticketId = commandService.create(new CreateTicketRequest("Original", "Body", null, null)).id();
    }

    @AfterEach
    void tearDown() {
        IntegrationTestSupport.clearSecurityContext();
    }

    @Test
    void rejectBlankTitle() {
        TmsException ex = assertThrows(TmsException.class,
                () -> commandService.update(ticketId, new UpdateTicketRequest("   ", null, null, null, null)));
        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
    }

    @Test
    void anyUserMayChangeAssignee() {
        TicketDetailDto updated = commandService.update(
                ticketId, new UpdateTicketRequest(null, null, null, assignee.getId(), null));

        assertEquals(assignee.getId(), updated.assignee().id());
    }

    private User saveUser(String prefix, UserRole role) {
        User user = new User();
        String name = prefix + "_" + System.nanoTime();
        user.setUsername(name);
        user.setPasswordHash(passwordEncoder.encode("password1"));
        user.setDisplayName(name);
        user.setEmail(name + "@example.com");
        user.setRole(role);
        return userRepository.save(user);
    }
}
