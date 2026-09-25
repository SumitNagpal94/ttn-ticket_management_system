package com.tms.ticket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tms.common.enums.UserRole;
import com.tms.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class TicketControllerDetailTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private long ticketId;
    private long assigneeId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        String adminToken = IntegrationTestSupport.adminToken(mockMvc, objectMapper);
        assigneeId = IntegrationTestSupport.createUser(
                mockMvc, objectMapper, adminToken, "dev_" + suffix, UserRole.DEVELOPER, "dev_" + suffix + "@example.com");
        IntegrationTestSupport.createUser(
                mockMvc, objectMapper, adminToken, "user_" + suffix, UserRole.USER, "user_" + suffix + "@example.com");
        userToken = IntegrationTestSupport.login(mockMvc, objectMapper, "user_" + suffix, "password1");
        ticketId = IntegrationTestSupport.createTicket(mockMvc, objectMapper, userToken, "Detail ticket", "Body");
    }

    @Test
    void getTicketById() throws Exception {
        mockMvc.perform(get("/api/tickets/" + ticketId).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId))
                .andExpect(jsonPath("$.title").value("Detail ticket"));
    }

    @Test
    void patchUpdatesAssignee() throws Exception {
        mockMvc.perform(patch("/api/tickets/" + ticketId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assigneeId\":" + assigneeId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee.id").value(assigneeId));
    }
}
