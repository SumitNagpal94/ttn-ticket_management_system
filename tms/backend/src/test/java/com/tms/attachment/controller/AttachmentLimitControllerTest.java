package com.tms.attachment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tms.common.enums.UserRole;
import com.tms.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "tms.attachments.max-per-ticket=2")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AttachmentLimitControllerTest {

    private static final byte[] PNG_BYTES = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52
    };

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
                mockMvc, objectMapper, adminToken, "limit_" + suffix, UserRole.USER, "limit_" + suffix + "@example.com");
        userToken = IntegrationTestSupport.login(mockMvc, objectMapper, "limit_" + suffix, "password1");
        ticketId = IntegrationTestSupport.createTicket(mockMvc, objectMapper, userToken, "Limit ticket", "Body");
    }

    @Test
    void rejectWhenAttachmentLimitReached() throws Exception {
        upload("one.png");
        upload("two.png");

        mockMvc.perform(multipart("/api/tickets/" + ticketId + "/attachments")
                        .file(new MockMultipartFile("file", "three.png", "image/png", PNG_BYTES))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ATTACHMENT_LIMIT_EXCEEDED"));
    }

    private void upload(String filename) throws Exception {
        mockMvc.perform(multipart("/api/tickets/" + ticketId + "/attachments")
                        .file(new MockMultipartFile("file", filename, "image/png", PNG_BYTES))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated());
    }
}
