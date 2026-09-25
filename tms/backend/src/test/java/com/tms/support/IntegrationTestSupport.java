package com.tms.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tms.common.enums.UserRole;
import com.tms.common.security.AuthenticatedUser;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class IntegrationTestSupport {

    private IntegrationTestSupport() {
    }

    public static void setSecurityContext(Long userId, String username, UserRole role) {
        AuthenticatedUser principal = new AuthenticatedUser(userId, username, role);
        var auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    public static String login(MockMvc mockMvc, ObjectMapper objectMapper, String username, String password)
            throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    public static long createUser(
            MockMvc mockMvc, ObjectMapper objectMapper, String adminToken, String username, UserRole role, String email)
            throws Exception {
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
                                """, username, username, email, role.name())))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    public static long createTicket(
            MockMvc mockMvc, ObjectMapper objectMapper, String token, String title, String description)
            throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"title\":\"%s\",\"description\":\"%s\"}", title, description)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    public static void assignTicket(MockMvc mockMvc, String token, long ticketId, long assigneeId) throws Exception {
        mockMvc.perform(patch("/api/tickets/" + ticketId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assigneeId\":" + assigneeId + "}"))
                .andExpect(status().isOk());
    }

    public static String adminToken(MockMvc mockMvc, ObjectMapper objectMapper) throws Exception {
        return login(mockMvc, objectMapper, "admin", "password");
    }

    public static JsonNode parse(ObjectMapper objectMapper, MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
