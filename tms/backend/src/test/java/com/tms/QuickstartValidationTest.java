package com.tms;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Automated coverage for quickstart.md validation scenarios VS-1 through VS-9 (API portions).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class QuickstartValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String suffix;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        suffix = String.valueOf(System.nanoTime());
        adminToken = IntegrationTestSupport.adminToken(mockMvc, objectMapper);
    }

    @Test
    void vs1_adminLoginAndCreateUser() throws Exception {
        String username = "dev1_" + suffix;
        IntegrationTestSupport.createUser(
                mockMvc, objectMapper, adminToken, username, UserRole.DEVELOPER, username + "@example.com");
        String devToken = IntegrationTestSupport.login(mockMvc, objectMapper, username, "password1");
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + devToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("DEVELOPER"));
    }

    @Test
    void vs2_createTicketOpenStatus() throws Exception {
        String userToken = createUserAndLogin("vs2_" + suffix);
        mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"VS2\",\"description\":\"Test\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void vs3_assigneeChangeByAnyUser() throws Exception {
        long devId = IntegrationTestSupport.createUser(
                mockMvc, objectMapper, adminToken, "dev3_" + suffix, UserRole.DEVELOPER, "dev3_" + suffix + "@example.com");
        String userToken = createUserAndLogin("user3_" + suffix);
        long ticketId = IntegrationTestSupport.createTicket(mockMvc, objectMapper, userToken, "Assign", "Body");
        mockMvc.perform(patch("/api/tickets/" + ticketId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assigneeId\":" + devId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee.id").value(devId));
    }

    @Test
    void vs4_validTransitions() throws Exception {
        long devId = IntegrationTestSupport.createUser(
                mockMvc, objectMapper, adminToken, "dev4_" + suffix, UserRole.DEVELOPER, "dev4_" + suffix + "@example.com");
        IntegrationTestSupport.createUser(
                mockMvc, objectMapper, adminToken, "qa4_" + suffix, UserRole.QA, "qa4_" + suffix + "@example.com");
        String creatorToken = createUserAndLogin("creator4_" + suffix);
        String devToken = IntegrationTestSupport.login(mockMvc, objectMapper, "dev4_" + suffix, "password1");
        String qaToken = IntegrationTestSupport.login(mockMvc, objectMapper, "qa4_" + suffix, "password1");

        long ticketId = IntegrationTestSupport.createTicket(mockMvc, objectMapper, creatorToken, "VS4", "Flow");
        IntegrationTestSupport.assignTicket(mockMvc, creatorToken, ticketId, devId);

        transition(devToken, ticketId, "IN_PROGRESS").andExpect(status().isOk());
        transition(devToken, ticketId, "RESOLVED").andExpect(status().isOk());
        transition(creatorToken, ticketId, "CLOSED").andExpect(status().isOk());

        long ticket2 = IntegrationTestSupport.createTicket(mockMvc, objectMapper, creatorToken, "VS4b", "QA close");
        IntegrationTestSupport.assignTicket(mockMvc, creatorToken, ticket2, devId);
        transition(devToken, ticket2, "IN_PROGRESS").andExpect(status().isOk());
        transition(devToken, ticket2, "RESOLVED").andExpect(status().isOk());
        transition(qaToken, ticket2, "CLOSED").andExpect(status().isOk());
    }

    @Test
    void vs5_invalidTransitions() throws Exception {
        long devId = IntegrationTestSupport.createUser(
                mockMvc, objectMapper, adminToken, "dev5_" + suffix, UserRole.DEVELOPER, "dev5_" + suffix + "@example.com");
        String userToken = createUserAndLogin("user5_" + suffix);
        String devToken = IntegrationTestSupport.login(mockMvc, objectMapper, "dev5_" + suffix, "password1");
        long ticketId = IntegrationTestSupport.createTicket(mockMvc, objectMapper, userToken, "VS5", "Invalid");
        IntegrationTestSupport.assignTicket(mockMvc, userToken, ticketId, devId);

        transition(devToken, ticketId, "RESOLVED").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_TRANSITION"));
        transition(userToken, ticketId, "IN_PROGRESS").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        transition(devToken, ticketId, "IN_PROGRESS").andExpect(status().isOk());
        transition(devToken, ticketId, "RESOLVED").andExpect(status().isOk());
        transition(devToken, ticketId, "CLOSED").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void vs6_searchAndFilter() throws Exception {
        String userToken = createUserAndLogin("user6_" + suffix);
        IntegrationTestSupport.createTicket(mockMvc, objectMapper, userToken, "SearchAlpha" + suffix, "plain");
        IntegrationTestSupport.createTicket(mockMvc, objectMapper, userToken, "Other", "SearchBeta" + suffix);

        MvcResult search = mockMvc.perform(get("/api/tickets/grouped?q=SearchAlpha" + suffix)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = IntegrationTestSupport.parse(objectMapper, search);
        assertTrue(body.get("sections").toString().contains("SearchAlpha" + suffix));

        mockMvc.perform(get("/api/tickets/grouped").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    void vs7_paginationThirtyPerPage() throws Exception {
        String userToken = createUserAndLogin("user7_" + suffix);
        long userId = fetchUserId(userToken);
        for (int i = 0; i < 31; i++) {
            long id = IntegrationTestSupport.createTicket(mockMvc, objectMapper, userToken, "Page" + i, "bulk");
            IntegrationTestSupport.assignTicket(mockMvc, userToken, id, userId);
        }
        MvcResult page0 = mockMvc.perform(get("/api/tickets/grouped?assigneeId=" + userId + "&page=0")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(30, IntegrationTestSupport.parse(objectMapper, page0).get("size").asInt());
    }

    @Test
    void vs8_persistenceWithinSession() throws Exception {
        String userToken = createUserAndLogin("user8_" + suffix);
        long ticketId = IntegrationTestSupport.createTicket(mockMvc, objectMapper, userToken, "Persist", "Data");
        mockMvc.perform(post("/api/tickets/" + ticketId + "/comments")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"still here\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/tickets/" + ticketId).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments[0].body").value("still here"));
    }

    @Test
    void vs9_validationErrorOnEmptyTitle() throws Exception {
        String userToken = createUserAndLogin("user9_" + suffix);
        mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"description\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private String createUserAndLogin(String username) throws Exception {
        IntegrationTestSupport.createUser(
                mockMvc, objectMapper, adminToken, username, UserRole.USER, username + "@example.com");
        return IntegrationTestSupport.login(mockMvc, objectMapper, username, "password1");
    }

    private long fetchUserId(String token) throws Exception {
        MvcResult me = mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        return IntegrationTestSupport.parse(objectMapper, me).get("id").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions transition(String token, long id, String status)
            throws Exception {
        return mockMvc.perform(post("/api/tickets/" + id + "/transitions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetStatus\":\"" + status + "\"}"));
    }
}
