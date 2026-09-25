package com.tms.comment.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private long ticketId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        String adminToken = IntegrationTestSupport.adminToken(mockMvc, objectMapper);
        IntegrationTestSupport.createUser(
                mockMvc, objectMapper, adminToken, "user_" + suffix, UserRole.USER, "user_" + suffix + "@example.com");
        userToken = IntegrationTestSupport.login(mockMvc, objectMapper, "user_" + suffix, "password1");
        ticketId = IntegrationTestSupport.createTicket(mockMvc, objectMapper, userToken, "Comment ticket", "Body");
    }

    @Test
    void addCommentSuccess() throws Exception {
        mockMvc.perform(post("/api/tickets/" + ticketId + "/comments")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"Looks good\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body").value("Looks good"));
    }

    @Test
    void rejectEmptyComment() throws Exception {
        mockMvc.perform(post("/api/tickets/" + ticketId + "/comments")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
