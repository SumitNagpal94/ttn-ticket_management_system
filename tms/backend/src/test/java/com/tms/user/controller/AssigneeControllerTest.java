package com.tms.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tms.common.enums.UserRole;
import com.tms.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
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
@ActiveProfiles("dev")
class AssigneeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        String adminToken = IntegrationTestSupport.adminToken(mockMvc, objectMapper);
        IntegrationTestSupport.createUser(
                mockMvc, objectMapper, adminToken, "assignee_" + suffix, UserRole.DEVELOPER, "a_" + suffix + "@example.com");
        IntegrationTestSupport.createUser(
                mockMvc, objectMapper, adminToken, "user_" + suffix, UserRole.USER, "user_" + suffix + "@example.com");
        userToken = IntegrationTestSupport.login(mockMvc, objectMapper, "user_" + suffix, "password1");
    }

    @Test
    void listAssigneesRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/users/assignees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listAssigneesReturnsUsers() throws Exception {
        mockMvc.perform(get("/api/users/assignees").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].displayName").exists());
    }
}
