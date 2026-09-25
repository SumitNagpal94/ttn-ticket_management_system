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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AttachmentControllerTest {

    private static final byte[] PNG_BYTES = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52
    };

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userAToken;
    private String userBToken;
    private String adminToken;
    private long ticketId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        adminToken = IntegrationTestSupport.adminToken(mockMvc, objectMapper);
        IntegrationTestSupport.createUser(
                mockMvc, objectMapper, adminToken, "att_a_" + suffix, UserRole.USER, "a_" + suffix + "@example.com");
        IntegrationTestSupport.createUser(
                mockMvc, objectMapper, adminToken, "att_b_" + suffix, UserRole.USER, "b_" + suffix + "@example.com");
        userAToken = IntegrationTestSupport.login(mockMvc, objectMapper, "att_a_" + suffix, "password1");
        userBToken = IntegrationTestSupport.login(mockMvc, objectMapper, "att_b_" + suffix, "password1");
        ticketId = IntegrationTestSupport.createTicket(mockMvc, objectMapper, userAToken, "Attachment ticket", "Body");
    }

    @Test
    void uploadListAndDownload() throws Exception {
        String created = mockMvc.perform(multipart("/api/tickets/" + ticketId + "/attachments")
                        .file(new MockMultipartFile("file", "shot.png", "image/png", PNG_BYTES))
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalFilename").value("shot.png"))
                .andExpect(jsonPath("$.contentType").value("image/png"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long attachmentId = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(get("/api/tickets/" + ticketId + "/attachments")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + attachmentId + ")].originalFilename").value("shot.png"));

        mockMvc.perform(get("/api/tickets/" + ticketId + "/attachments/" + attachmentId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("shot.png")));
    }

    @Test
    void rejectInvalidFileType() throws Exception {
        mockMvc.perform(multipart("/api/tickets/" + ticketId + "/attachments")
                        .file(new MockMultipartFile("file", "bad.exe", "application/octet-stream", new byte[] {0x4D, 0x5A}))
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FILE_TYPE"));
    }

    @Test
    void uploaderCanDeleteOwnAttachment() throws Exception {
        long attachmentId = uploadAttachment("mine.png");

        mockMvc.perform(delete("/api/tickets/" + ticketId + "/attachments/" + attachmentId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void otherUserCannotDelete() throws Exception {
        long attachmentId = uploadAttachment("mine.png");

        mockMvc.perform(delete("/api/tickets/" + ticketId + "/attachments/" + attachmentId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void adminCanDeleteAnyAttachment() throws Exception {
        long attachmentId = uploadAttachment("mine.png");

        mockMvc.perform(delete("/api/tickets/" + ticketId + "/attachments/" + attachmentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    private long uploadAttachment(String filename) throws Exception {
        String created = mockMvc.perform(multipart("/api/tickets/" + ticketId + "/attachments")
                        .file(new MockMultipartFile("file", filename, "image/png", PNG_BYTES))
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(created).get("id").asLong();
    }

    @Test
    void rejectFileTooLarge() throws Exception {
        byte[] oversized = new byte[10 * 1024 * 1024 + 1];
        oversized[0] = (byte) 0x89;
        oversized[1] = 0x50;
        oversized[2] = 0x4E;
        oversized[3] = 0x47;

        mockMvc.perform(multipart("/api/tickets/" + ticketId + "/attachments")
                        .file(new MockMultipartFile("file", "big.png", "image/png", oversized))
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FILE_TOO_LARGE"));
    }

    @Test
    void unauthenticatedUploadDenied() throws Exception {
        mockMvc.perform(multipart("/api/tickets/" + ticketId + "/attachments")
                        .file(new MockMultipartFile("file", "shot.png", "image/png", PNG_BYTES)))
                .andExpect(status().isUnauthorized());
    }
}
