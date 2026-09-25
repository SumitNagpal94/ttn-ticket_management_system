package com.tms.attachment.model.dto;

import com.tms.common.dto.UserSummaryDto;

import java.time.Instant;

public record AttachmentDto(
        Long id,
        String originalFilename,
        String contentType,
        long fileSize,
        UserSummaryDto uploadedBy,
        Instant createdAt) {
}
