package com.tms.user.controller;

import com.tms.common.dto.UserSummaryDto;
import com.tms.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class AssigneeController {

    private final UserService userService;

    public AssigneeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/assignees")
    public List<UserSummaryDto> assignees() {
        return userService.listAssignees().stream()
                .map(u -> new UserSummaryDto(u.id(), u.displayName()))
                .toList();
    }
}
