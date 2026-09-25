package com.tms.ticket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tms.common.enums.UserRole;
import com.tms.support.IntegrationTestSupport;
import com.tms.ticket.model.dto.GroupedTicketResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class TicketQueryServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketQueryService queryService;

    @Autowired
    private TicketCommandService commandService;

    private String suffix;
    private String userToken;
    private long userId;
    private long otherUserId;

    @BeforeEach
    void setUp() throws Exception {
        suffix = String.valueOf(System.nanoTime());
        String adminToken = IntegrationTestSupport.adminToken(mockMvc, objectMapper);
        userId = IntegrationTestSupport.createUser(
                mockMvc, objectMapper, adminToken, "user_" + suffix, UserRole.USER, "user_" + suffix + "@example.com");
        otherUserId = IntegrationTestSupport.createUser(
                mockMvc, objectMapper, adminToken, "other_" + suffix, UserRole.USER, "other_" + suffix + "@example.com");
        userToken = IntegrationTestSupport.login(mockMvc, objectMapper, "user_" + suffix, "password1");
    }

    @Test
    void assigneeIdFilterLimitsToSelectedUser() throws Exception {
        long mine = IntegrationTestSupport.createTicket(mockMvc, objectMapper, userToken, "Mine", "Assigned to me");
        IntegrationTestSupport.assignTicket(mockMvc, userToken, mine, userId);

        String otherToken = IntegrationTestSupport.login(mockMvc, objectMapper, "other_" + suffix, "password1");
        long theirs = IntegrationTestSupport.createTicket(mockMvc, objectMapper, otherToken, "Theirs", "Not mine");
        IntegrationTestSupport.assignTicket(mockMvc, otherToken, theirs, otherUserId);

        IntegrationTestSupport.setSecurityContext(userId, "user_" + suffix, UserRole.USER);
        GroupedTicketResponse grouped = queryService.findGrouped(userId, null, 0, null);

        long openCount = grouped.sections().stream()
                .filter(s -> s.status().name().equals("OPEN"))
                .mapToLong(s -> s.tickets().size())
                .sum();
        assertTrue(openCount >= 1);
        assertTrue(grouped.sections().stream()
                .flatMap(s -> s.tickets().stream())
                .allMatch(t -> t.assignee() != null && t.assignee().id().equals(userId)));
        IntegrationTestSupport.clearSecurityContext();
    }

    @Test
    void pageSizeDefaultsToThirty() {
        IntegrationTestSupport.setSecurityContext(userId, "user_" + suffix, UserRole.USER);
        GroupedTicketResponse grouped = queryService.findGrouped(null, null, 0, null);
        assertEquals(30, grouped.size());
        IntegrationTestSupport.clearSecurityContext();
    }

    @Test
    void searchMatchesTitleAndDescriptionOnly() throws Exception {
        IntegrationTestSupport.createTicket(mockMvc, objectMapper, userToken, "AlphaUnique" + suffix, "plain body");
        IntegrationTestSupport.createTicket(mockMvc, objectMapper, userToken, "Other", "BetaUnique" + suffix + " text");

        IntegrationTestSupport.setSecurityContext(userId, "user_" + suffix, UserRole.USER);
        GroupedTicketResponse byTitle = queryService.findGrouped(null, "AlphaUnique" + suffix, 0, null);
        GroupedTicketResponse byDesc = queryService.findGrouped(null, "BetaUnique" + suffix, 0, null);

        assertTrue(byTitle.sections().stream().flatMap(s -> s.tickets().stream()).anyMatch(t -> t.title().contains("AlphaUnique")));
        assertTrue(byDesc.sections().stream().flatMap(s -> s.tickets().stream()).anyMatch(t -> t.title().equals("Other")));
        IntegrationTestSupport.clearSecurityContext();
    }

    @Test
    void assigneeIdFilter() throws Exception {
        long ticketId = IntegrationTestSupport.createTicket(mockMvc, objectMapper, userToken, "FilterMe", "desc");
        IntegrationTestSupport.assignTicket(mockMvc, userToken, ticketId, userId);

        IntegrationTestSupport.setSecurityContext(userId, "user_" + suffix, UserRole.USER);
        GroupedTicketResponse grouped = queryService.findGrouped(userId, null, 0, null);

        assertTrue(grouped.sections().stream()
                .flatMap(s -> s.tickets().stream())
                .allMatch(t -> t.assignee() != null && t.assignee().id().equals(userId)));
        IntegrationTestSupport.clearSecurityContext();
    }
}
