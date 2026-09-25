package com.tms.attachment.service;

import com.tms.attachment.model.dto.AttachmentDto;
import com.tms.attachment.model.entity.Attachment;
import com.tms.attachment.repository.AttachmentRepository;
import com.tms.common.config.TmsConfig;
import com.tms.common.dto.UserSummaryDto;
import com.tms.common.enums.ErrorCode;
import com.tms.common.enums.UserRole;
import com.tms.common.exception.TmsException;
import com.tms.common.security.AuthenticatedUser;
import com.tms.common.util.SecurityUtil;
import com.tms.ticket.model.entity.Ticket;
import com.tms.ticket.repository.TicketRepository;
import com.tms.user.model.entity.User;
import com.tms.user.repository.UserRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AttachmentStorageService storageService;
    private final AttachmentContentTypeValidator contentTypeValidator;
    private final TmsConfig tmsConfig;

    public AttachmentService(
            AttachmentRepository attachmentRepository,
            TicketRepository ticketRepository,
            UserRepository userRepository,
            AttachmentStorageService storageService,
            AttachmentContentTypeValidator contentTypeValidator,
            TmsConfig tmsConfig) {
        this.attachmentRepository = attachmentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.contentTypeValidator = contentTypeValidator;
        this.tmsConfig = tmsConfig;
    }

    @Transactional(readOnly = true)
    public List<AttachmentDto> list(Long ticketId) {
        requireAuthenticated();
        requireTicket(ticketId);
        return attachmentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public AttachmentDto upload(Long ticketId, MultipartFile file) {
        AuthenticatedUser actor = requireAuthenticated();
        Ticket ticket = requireTicket(ticketId);
        User uploader = requireUser(actor.userId());

        if (file == null || file.isEmpty()) {
            throw new TmsException(ErrorCode.VALIDATION_ERROR, "File is required");
        }
        if (file.getSize() > tmsConfig.getAttachments().getMaxFileSizeBytes()) {
            throw new TmsException(
                    ErrorCode.FILE_TOO_LARGE,
                    "File exceeds maximum size of 10 MB");
        }
        if (attachmentRepository.countByTicketId(ticketId) >= tmsConfig.getAttachments().getMaxPerTicket()) {
            throw new TmsException(
                    ErrorCode.ATTACHMENT_LIMIT_EXCEEDED,
                    "Ticket has reached the maximum of 20 attachments");
        }

        String contentType;
        try (InputStream input = file.getInputStream()) {
            contentType = contentTypeValidator.detectContentType(input);
        } catch (IOException ex) {
            throw new TmsException(ErrorCode.VALIDATION_ERROR, "Unable to read uploaded file");
        }
        if (contentType == null) {
            throw new TmsException(
                    ErrorCode.INVALID_FILE_TYPE,
                    "Only JPEG, PNG, GIF, WebP, and PDF files are allowed");
        }

        String storageKey = UUID.randomUUID().toString();
        try (InputStream input = file.getInputStream()) {
            storageService.store(storageKey, input);
        } catch (IOException ex) {
            throw new TmsException(ErrorCode.VALIDATION_ERROR, "Unable to store uploaded file");
        }

        Attachment attachment = new Attachment();
        attachment.setTicket(ticket);
        attachment.setUploadedBy(uploader);
        attachment.setOriginalFilename(sanitizeFilename(file.getOriginalFilename()));
        attachment.setContentType(contentType);
        attachment.setFileSize(file.getSize());
        attachment.setStorageKey(storageKey);
        Attachment saved = attachmentRepository.save(attachment);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> download(Long ticketId, Long attachmentId) {
        requireAuthenticated();
        requireTicket(ticketId);
        Attachment attachment = attachmentRepository.findByIdAndTicketId(attachmentId, ticketId)
                .orElseThrow(() -> new TmsException(ErrorCode.NOT_FOUND, "Attachment not found"));
        Path path = storageService.resolvePath(attachment.getStorageKey());
        if (!Files.exists(path)) {
            throw new TmsException(ErrorCode.NOT_FOUND, "Attachment file not found");
        }
        try {
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(path));
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getContentType()))
                    .header(
                            "Content-Disposition",
                            "attachment; filename=\"" + attachment.getOriginalFilename() + "\"")
                    .contentLength(attachment.getFileSize())
                    .body(resource);
        } catch (IOException ex) {
            throw new TmsException(ErrorCode.NOT_FOUND, "Attachment file not found");
        }
    }

    @Transactional
    public void delete(Long ticketId, Long attachmentId) {
        AuthenticatedUser actor = requireAuthenticated();
        requireTicket(ticketId);
        Attachment attachment = attachmentRepository.findByIdAndTicketId(attachmentId, ticketId)
                .orElseThrow(() -> new TmsException(ErrorCode.NOT_FOUND, "Attachment not found"));
        if (!canDelete(actor, attachment)) {
            throw new TmsException(ErrorCode.FORBIDDEN, "You may only delete your own attachments");
        }
        try {
            storageService.delete(attachment.getStorageKey());
        } catch (IOException ex) {
            throw new TmsException(ErrorCode.VALIDATION_ERROR, "Unable to delete attachment file");
        }
        attachmentRepository.delete(attachment);
    }

    private boolean canDelete(AuthenticatedUser actor, Attachment attachment) {
        if (actor.role() == UserRole.ADMIN) {
            return true;
        }
        return attachment.getUploadedBy().getId().equals(actor.userId());
    }

    private AuthenticatedUser requireAuthenticated() {
        AuthenticatedUser actor = SecurityUtil.currentUser();
        if (actor == null) {
            throw new TmsException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        if (actor.userId() == null && actor.role() != UserRole.ADMIN) {
            throw new TmsException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        return actor;
    }

    private Ticket requireTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TmsException(ErrorCode.NOT_FOUND, "Ticket not found"));
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new TmsException(ErrorCode.NOT_FOUND, "User not found"));
    }

    private AttachmentDto toDto(Attachment attachment) {
        User uploader = attachment.getUploadedBy();
        return new AttachmentDto(
                attachment.getId(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getFileSize(),
                new UserSummaryDto(uploader.getId(), uploader.getDisplayName()),
                attachment.getCreatedAt());
    }

    static String sanitizeFilename(String original) {
        if (original == null || original.isBlank()) {
            return "upload";
        }
        String name = Path.of(original).getFileName().toString().replaceAll("[\\r\\n\"]", "");
        return name.isBlank() ? "upload" : name;
    }
}
