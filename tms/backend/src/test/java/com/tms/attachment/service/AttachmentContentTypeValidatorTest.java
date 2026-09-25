package com.tms.attachment.service;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AttachmentContentTypeValidatorTest {

    private final AttachmentContentTypeValidator validator = new AttachmentContentTypeValidator();

    @Test
    void detectsPdf() throws Exception {
        assertEquals("application/pdf", validator.detectContentType(new ByteArrayInputStream("%PDF-1.4".getBytes())));
    }

    @Test
    void detectsPng() throws Exception {
        byte[] png = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        assertEquals("image/png", validator.detectContentType(new ByteArrayInputStream(png)));
    }

    @Test
    void detectsJpeg() throws Exception {
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        assertEquals("image/jpeg", validator.detectContentType(new ByteArrayInputStream(jpeg)));
    }

    @Test
    void detectsGif() throws Exception {
        assertEquals("image/gif", validator.detectContentType(new ByteArrayInputStream("GIF89a".getBytes())));
    }

    @Test
    void detectsWebp() throws Exception {
        byte[] webp = "RIFF".getBytes();
        byte[] chunk = new byte[12];
        System.arraycopy(webp, 0, chunk, 0, 4);
        System.arraycopy("WEBP".getBytes(), 0, chunk, 8, 4);
        assertEquals("image/webp", validator.detectContentType(new ByteArrayInputStream(chunk)));
    }

    @Test
    void rejectsUnknown() throws Exception {
        assertNull(validator.detectContentType(new ByteArrayInputStream("hello".getBytes())));
    }
}
