package com.tms.comment.service;

import com.tms.common.enums.ErrorCode;
import com.tms.common.exception.TmsException;
import com.tms.common.security.AuthenticatedUser;
import com.tms.common.util.SecurityUtil;
import com.tms.comment.model.dto.CreateCommentRequest;
import com.tms.ticket.model.dto.CommentDto;
import com.tms.comment.model.entity.Comment;
import com.tms.comment.repository.CommentRepository;
import com.tms.common.dto.UserSummaryDto;
import com.tms.ticket.model.entity.Ticket;
import com.tms.ticket.repository.TicketRepository;
import com.tms.user.model.entity.User;
import com.tms.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public CommentService(
            CommentRepository commentRepository,
            TicketRepository ticketRepository,
            UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommentDto addComment(Long ticketId, CreateCommentRequest request) {
        AuthenticatedUser actor = SecurityUtil.currentUser();
        if (actor == null || actor.userId() == null) {
            throw new TmsException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TmsException(ErrorCode.NOT_FOUND, "Ticket not found"));
        User author = userRepository.findById(actor.userId())
                .orElseThrow(() -> new TmsException(ErrorCode.NOT_FOUND, "User not found"));
        String body = request.body() == null ? "" : request.body().trim();
        if (body.isEmpty()) {
            throw new TmsException(ErrorCode.VALIDATION_ERROR, "Comment body is required");
        }
        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setBody(body);
        Comment saved = commentRepository.save(comment);
        return new CommentDto(
                saved.getId(),
                saved.getBody(),
                new UserSummaryDto(author.getId(), author.getDisplayName()),
                saved.getCreatedAt());
    }
}
