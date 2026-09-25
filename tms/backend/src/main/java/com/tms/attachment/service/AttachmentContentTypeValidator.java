package com.tms.attachment.service;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@Component
public class AttachmentContentTypeValidator {

    public String detectContentType(InputStream input) throws IOException {
        byte[] header = new byte[12];
        int read = input.read(header);
        if (read < 4) {
            return null;
        }
        if (startsWith(header, read, new byte[] {0x25, 0x50, 0x44, 0x46})) {
            return "application/pdf";
        }
        if (read >= 3 && header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
            return "image/jpeg";
        }
        if (startsWith(header, read, new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47})) {
            return "image/png";
        }
        if (read >= 6 && header[0] == 'G' && header[1] == 'I' && header[2] == 'F') {
            return "image/gif";
        }
        if (read >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    private boolean startsWith(byte[] data, int length, byte[] prefix) {
        if (length < prefix.length) {
            return false;
        }
        return Arrays.equals(Arrays.copyOf(data, prefix.length), prefix);
    }
}
