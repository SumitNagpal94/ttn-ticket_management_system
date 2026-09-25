package com.tms.ticket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tms.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class TicketControllerGroupedRouteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void groupedWithAdminToken() throws Exception {
        String token = IntegrationTestSupport.login(mockMvc, objectMapper, "admin", "Admin@123");
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/tickets/999").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/tickets/grouped?page=0")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections").isArray());
    }

    @Test
    void nonNumericIdReturnsNotFound() throws Exception {
        String token = IntegrationTestSupport.login(mockMvc, objectMapper, "admin", "Admin@123");
        mockMvc.perform(get("/api/tickets/foo").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
