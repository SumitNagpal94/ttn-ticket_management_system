package com.tms.comment.controller;

import com.tms.comment.model.dto.CreateCommentRequest;
import com.tms.comment.service.CommentService;
import com.tms.ticket.model.dto.CommentDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto add(
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateCommentRequest request) {
        return commentService.addComment(ticketId, request);
    }
}
