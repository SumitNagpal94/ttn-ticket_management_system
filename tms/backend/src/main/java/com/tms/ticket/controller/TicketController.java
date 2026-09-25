package com.tms.ticket.controller;

import com.tms.ticket.model.dto.CreateTicketRequest;
import com.tms.ticket.model.dto.GroupedTicketResponse;
import com.tms.ticket.model.dto.TicketDetailDto;
import com.tms.ticket.model.dto.TransitionRequest;
import com.tms.ticket.model.dto.UpdateTicketRequest;
import com.tms.ticket.service.TicketCommandService;
import com.tms.ticket.service.TicketQueryService;
import com.tms.ticket.service.TicketTransitionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketCommandService commandService;
    private final TicketQueryService queryService;
    private final TicketTransitionService transitionService;

    public TicketController(
            TicketCommandService commandService,
            TicketQueryService queryService,
            TicketTransitionService transitionService) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.transitionService = transitionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDetailDto create(@Valid @RequestBody CreateTicketRequest request) {
        return commandService.create(request);
    }

    @GetMapping("/grouped")
    public GroupedTicketResponse grouped(
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size) {
        return queryService.findGrouped(assigneeId, q, page, size);
    }

    @GetMapping("/{id:\\d+}")
    public TicketDetailDto get(@PathVariable Long id) {
        return queryService.findById(id);
    }

    @PatchMapping("/{id:\\d+}")
    public TicketDetailDto update(@PathVariable Long id, @Valid @RequestBody UpdateTicketRequest request) {
        return commandService.update(id, request);
    }

    @PostMapping("/{id:\\d+}/transitions")
    public TicketDetailDto transition(@PathVariable Long id, @Valid @RequestBody TransitionRequest request) {
        return transitionService.transition(id, request.targetStatus());
    }
}
