package com.tms.comment.service;

import com.tms.common.enums.ErrorCode;
import com.tms.common.enums.UserRole;
import com.tms.common.exception.TmsException;
import com.tms.comment.model.dto.CreateCommentRequest;
import com.tms.support.IntegrationTestSupport;
import com.tms.ticket.model.dto.CreateTicketRequest;
import com.tms.ticket.service.TicketCommandService;
import com.tms.user.model.entity.User;
import com.tms.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("dev")
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private TicketCommandService ticketCommandService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long ticketId;

    @BeforeEach
    void setUp() {
        User user = new User();
        String name = "commenter_" + System.nanoTime();
        user.setUsername(name);
        user.setPasswordHash(passwordEncoder.encode("password1"));
        user.setDisplayName(name);
        user.setEmail(name + "@example.com");
        user.setRole(UserRole.USER);
        user = userRepository.save(user);
        IntegrationTestSupport.setSecurityContext(user.getId(), user.getUsername(), UserRole.USER);
        ticketId = ticketCommandService.create(new CreateTicketRequest("T", "D", null, null)).id();
    }

    @AfterEach
    void tearDown() {
        IntegrationTestSupport.clearSecurityContext();
    }

    @Test
    void rejectEmptyBody() {
        TmsException ex = assertThrows(TmsException.class,
                () -> commentService.addComment(ticketId, new CreateCommentRequest("   ")));
        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
    }
}
