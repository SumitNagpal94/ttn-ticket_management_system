package com.tms.common.security;

import com.tms.common.config.TmsConfig;
import com.tms.common.enums.UserRole;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminAuthenticationProvider implements AuthenticationProvider {

    private final TmsConfig tmsConfig;
    private final PasswordEncoder passwordEncoder;

    public AdminAuthenticationProvider(TmsConfig tmsConfig, PasswordEncoder passwordEncoder) {
        this.tmsConfig = tmsConfig;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        if (!username.equals(tmsConfig.getAdmin().getUsername())) {
            return null;
        }
        String hash = tmsConfig.getAdmin().getPasswordHash();
        if (hash == null || hash.isBlank() || !passwordEncoder.matches(password, hash)) {
            throw new BadCredentialsException("Invalid admin credentials");
        }
        var principal = new AuthenticatedUser(null, username, UserRole.ADMIN);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public boolean isAdminUsername(String username) {
        return username.equals(tmsConfig.getAdmin().getUsername());
    }
}
