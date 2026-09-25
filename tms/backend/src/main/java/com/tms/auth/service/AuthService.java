package com.tms.auth.service;

import com.tms.auth.model.dto.LoginRequest;
import com.tms.auth.model.dto.LoginResponse;
import com.tms.auth.model.dto.MeResponse;
import com.tms.common.enums.ErrorCode;
import com.tms.common.enums.UserRole;
import com.tms.common.exception.TmsException;
import com.tms.common.security.AdminAuthenticationProvider;
import com.tms.common.security.AuthenticatedUser;
import com.tms.common.security.JwtService;
import com.tms.common.util.SecurityUtil;
import com.tms.user.model.entity.User;
import com.tms.user.repository.UserRepository;
import com.tms.user.security.DatabaseUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final AdminAuthenticationProvider adminAuthenticationProvider;
    private final DatabaseUserDetailsService databaseUserDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            JwtService jwtService,
            AdminAuthenticationProvider adminAuthenticationProvider,
            DatabaseUserDetailsService databaseUserDetailsService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.adminAuthenticationProvider = adminAuthenticationProvider;
        this.databaseUserDetailsService = databaseUserDetailsService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        if (adminAuthenticationProvider.isAdminUsername(request.username())) {
            var auth = adminAuthenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            AuthenticatedUser principal = (AuthenticatedUser) auth.getPrincipal();
            String token = jwtService.generateToken(principal.username(), principal.role(), null);
            return new LoginResponse(token, principal.username(), "Administrator", principal.role());
        }
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new TmsException(ErrorCode.INVALID_CREDENTIALS, "Invalid username or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new TmsException(ErrorCode.INVALID_CREDENTIALS, "Invalid username or password");
        }
        String token = jwtService.generateToken(user.getUsername(), user.getRole(), user.getId());
        return new LoginResponse(token, user.getUsername(), user.getDisplayName(), user.getRole());
    }

    public MeResponse me() {
        AuthenticatedUser actor = SecurityUtil.currentUser();
        if (actor == null) {
            throw new TmsException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        if (isPropertiesAdmin(actor)) {
            return new MeResponse(null, actor.username(), "Administrator", null, UserRole.ADMIN);
        }
        User user = databaseUserDetailsService.loadEntity(actor.username());
        return new MeResponse(user.getId(), user.getUsername(), user.getDisplayName(), user.getEmail(), user.getRole());
    }

    private boolean isPropertiesAdmin(AuthenticatedUser actor) {
        return adminAuthenticationProvider.isAdminUsername(actor.username())
                && (actor.userId() == null || actor.userId() <= 0);
    }
}
