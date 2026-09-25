package com.tms.attachment.controller;

import com.tms.attachment.model.dto.AttachmentDto;
import com.tms.attachment.service.AttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping
    public List<AttachmentDto> list(@PathVariable Long ticketId) {
        return attachmentService.list(ticketId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentDto upload(
            @PathVariable Long ticketId,
            @RequestParam("file") MultipartFile file) {
        return attachmentService.upload(ticketId, file);
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<Resource> download(
            @PathVariable Long ticketId,
            @PathVariable Long attachmentId) {
        return attachmentService.download(ticketId, attachmentId);
    }

    @DeleteMapping("/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long ticketId, @PathVariable Long attachmentId) {
        attachmentService.delete(ticketId, attachmentId);
    }
}
