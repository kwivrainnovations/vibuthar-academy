package com.academy.project.serviceImplementation.auth;

import com.academy.project.dto.auth.AuthResponse;
import com.academy.project.dto.auth.LoginRequest;
import com.academy.project.dto.auth.RefreshTokenRequest;
import com.academy.project.dto.register.RegisterRequest;
import com.academy.project.dto.response.UserResponse;
import com.academy.project.entity.user.User;
import com.academy.project.entity.user.UserRole;
import com.academy.project.entity.user.UserSession;
import com.academy.project.entity.user.UserStatus;
import com.academy.project.exception.ApiException;
import com.academy.project.repository.user.UserRepository;
import com.academy.project.repository.user.UserSessionRepository;
import com.academy.project.security.JwtTokenProvider;
import com.academy.project.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthServiceImplementation implements AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getEmail() == null && request.getPhone() == null) {
            throw ApiException.badRequest("Either email or phone is required");
        }
        if (request.getEmail() != null && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw ApiException.conflict("An account with this email already exists");
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw ApiException.conflict("An account with this phone number already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(resolveRole(request.getRole()))
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        return issueTokens(user, null, null);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
            );
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new BadCredentialsException("Invalid email/phone or password");
        }

        User user = userRepository.findActiveByIdentifier(request.getIdentifier())
                .orElseThrow(() -> new BadCredentialsException("Invalid email/phone or password"));

        if (!user.isActive()) {
            throw ApiException.forbidden("This account is inactive. Contact support.");
        }

        return issueTokens(user, request.getDeviceInfo(), ipAddress);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String hashed = hashToken(request.getRefreshToken());

        UserSession session = userSessionRepository.findByRefreshTokenHash(hashed)
                .orElseThrow(() -> ApiException.unauthorized("Invalid or expired refresh token"));

        if (!session.isValid()) {
            throw ApiException.unauthorized("Invalid or expired refresh token");
        }

        User user = userRepository.findById(session.getUserId())
                .filter(User::isActive)
                .orElseThrow(() -> ApiException.unauthorized("Account no longer active"));

        // Rotate: revoke the used refresh token and issue a brand new pair.
        session.setRevokedAt(LocalDateTime.now());
        userSessionRepository.save(session);

        return issueTokens(user, session.getDeviceInfo(), session.getIpAddress());
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        String hashed = hashToken(request.getRefreshToken());
        userSessionRepository.findByRefreshTokenHash(hashed).ifPresent(session -> {
            session.setRevokedAt(LocalDateTime.now());
            userSessionRepository.save(session);
        });
    }

    // ---------- helpers ----------

    private AuthResponse issueTokens(User user, String deviceInfo, String ipAddress) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String rawRefreshToken = jwtTokenProvider.generateOpaqueRefreshToken();

        UserSession session = UserSession.builder()
                .userId(user.getId())
                .refreshTokenHash(hashToken(rawRefreshToken))
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpirationMs())))
                .build();
        userSessionRepository.save(session);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .user(UserResponse.fromEntity(user))
                .build();
    }

    /**
     * Refresh tokens are stored hashed (never in plaintext) so a DB read/leak can't be replayed
     * as a live session - same principle as password_hash on the users table.
     *
     * BCrypt is deliberately NOT used here: it salts randomly per call, which would make
     * "look this token up by its hash" (a unique-index equality lookup) impossible. The raw
     * token already has 512 bits of entropy from SecureRandom, so a fast deterministic digest
     * (SHA-256) is the correct and standard choice for opaque refresh/session tokens.
     */
    private String hashToken(String rawToken) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
    private UserRole resolveRole(UserRole requestedRole) {
        if (requestedRole != null && isCurrentUserAdmin()) {
            return requestedRole;
        }
        return UserRole.STUDENT;
    }

    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
    }
}
