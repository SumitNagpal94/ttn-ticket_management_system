package com.tms.ticket.service;

import com.tms.common.enums.TicketPriority;
import com.tms.common.enums.TicketStatus;
import com.tms.common.enums.UserRole;
import com.tms.support.IntegrationTestSupport;
import com.tms.ticket.model.dto.CreateTicketRequest;
import com.tms.ticket.model.dto.TicketDetailDto;
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
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("dev")
class TicketCommandServiceTest {

    @Autowired
    private TicketCommandService commandService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User creator;

    @BeforeEach
    void setUp() {
        creator = new User();
        creator.setUsername("creator_" + System.nanoTime());
        creator.setPasswordHash(passwordEncoder.encode("password1"));
        creator.setDisplayName("Creator");
        creator.setEmail(creator.getUsername() + "@example.com");
        creator.setRole(UserRole.USER);
        creator = userRepository.save(creator);
        IntegrationTestSupport.setSecurityContext(creator.getId(), creator.getUsername(), UserRole.USER);
    }

    @AfterEach
    void tearDown() {
        IntegrationTestSupport.clearSecurityContext();
    }

    @Test
    void createSetsOpenAndMediumDefault() {
        TicketDetailDto ticket = commandService.create(new CreateTicketRequest("Title", "Description", null, null));

        assertEquals(TicketStatus.OPEN, ticket.status());
        assertEquals(TicketPriority.MEDIUM, ticket.priority());
        assertEquals(creator.getId(), ticket.createdBy().id());
        assertNull(ticket.assignee());
    }
}
