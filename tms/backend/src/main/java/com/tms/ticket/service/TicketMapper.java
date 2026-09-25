package com.tms.ticket.service;

import com.tms.comment.model.entity.Comment;
import com.tms.comment.repository.CommentRepository;
import com.tms.common.dto.UserSummaryDto;
import com.tms.ticket.model.dto.CommentDto;
import com.tms.ticket.model.dto.TicketDetailDto;
import com.tms.ticket.model.dto.TicketSummaryDto;
import com.tms.ticket.model.entity.Ticket;
import com.tms.user.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TicketMapper {

    private final CommentRepository commentRepository;

    public TicketMapper(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public TicketSummaryDto toSummary(Ticket ticket) {
        return new TicketSummaryDto(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getStatus(),
                ticket.getPriority(),
                toUserSummary(ticket.getAssignee()),
                toUserSummary(ticket.getCreatedBy()),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }

    public TicketDetailDto toDetail(Ticket ticket) {
        List<CommentDto> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId())
                .stream()
                .map(this::toCommentDto)
                .toList();
        return new TicketDetailDto(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                toUserSummary(ticket.getAssignee()),
                toUserSummary(ticket.getCreatedBy()),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                comments);
    }

    private CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getBody(),
                toUserSummary(comment.getAuthor()),
                comment.getCreatedAt());
    }

    private UserSummaryDto toUserSummary(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryDto(user.getId(), user.getDisplayName());
    }
}
