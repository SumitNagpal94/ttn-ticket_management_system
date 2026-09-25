package com.tms.ticket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class TicketTransitionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String devToken;
    private String userToken;
    private Long devUserId;
    private Long ticketId;

    private String suffix;

    @BeforeEach
    void setUp() throws Exception {
        suffix = String.valueOf(System.nanoTime());
        String adminToken = login("admin", "password");
        devUserId = createUser(adminToken, "dev1_" + suffix, "DEVELOPER", "dev1_" + suffix + "@example.com");
        createUser(adminToken, "creator1_" + suffix, "USER", "creator1_" + suffix + "@example.com");
        devToken = login("dev1_" + suffix, "password1");
        userToken = login("creator1_" + suffix, "password1");
        ticketId = createTicket(userToken, "Bug", "Something broke");
        assignTicket(devToken, ticketId, devUserId);
    }

    @Test
    void validOpenToInProgress() throws Exception {
        transition(devToken, ticketId, "IN_PROGRESS").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void invalidOpenToResolved() throws Exception {
        transition(devToken, ticketId, "RESOLVED").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_TRANSITION"));
    }

    @Test
    void creatorCanCloseResolved() throws Exception {
        transition(devToken, ticketId, "IN_PROGRESS").andExpect(status().isOk());
        transition(devToken, ticketId, "RESOLVED").andExpect(status().isOk());
        transition(userToken, ticketId, "CLOSED").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void userCannotStartProgress() throws Exception {
        transition(userToken, ticketId, "IN_PROGRESS").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    private long createUser(String adminToken, String username, String role, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "username": "%s",
                                  "password": "password1",
                                  "displayName": "%s",
                                  "email": "%s",
                                  "role": "%s"
                                }
                                """, username, username, email, role)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private Long createTicket(String token, String title, String description) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"title\":\"%s\",\"description\":\"%s\"}", title, description)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private void assignTicket(String token, Long id, long assigneeId) throws Exception {
        mockMvc.perform(patch("/api/tickets/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assigneeId\":" + assigneeId + "}"))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions transition(String token, Long id, String status)
            throws Exception {
        return mockMvc.perform(post("/api/tickets/" + id + "/transitions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"" + status + "\"}"));
    }
}
