package com.tms.attachment.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttachmentServiceTest {

    @Test
    void sanitizeFilenameStripsPath() {
        assertEquals("file.png", AttachmentService.sanitizeFilename("../../etc/file.png"));
        assertEquals("upload", AttachmentService.sanitizeFilename(""));
    }
}
