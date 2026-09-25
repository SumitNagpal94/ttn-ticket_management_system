package com.tms.attachment.repository;

import com.tms.attachment.model.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    long countByTicketId(Long ticketId);

    Optional<Attachment> findByIdAndTicketId(Long id, Long ticketId);
}
