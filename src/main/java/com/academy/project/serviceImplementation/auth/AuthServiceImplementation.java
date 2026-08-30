package com.academy.project.serviceImplementation.auth;

import com.academy.project.dto.auth.AuthResponse;
import com.academy.project.dto.auth.ForgotPasswordRequest;
import com.academy.project.dto.auth.LoginRequest;
import com.academy.project.dto.auth.RefreshTokenRequest;
import com.academy.project.dto.auth.ResetPasswordRequest;
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
import com.academy.project.security.SecurityUtils;
import com.academy.project.service.auth.AuthService;
import com.academy.project.util.PhoneUtils;
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
        if ( request.getPhone() == null) {
            throw ApiException.badRequest(" Phone Number is required");
        }
        if (userRepository.existsByPhoneNormalized(request.getPhone())) {
            throw ApiException.conflict("An account with this phone number already exists");
        }

        String normalizedPhone = request.getPhone() != null ? PhoneUtils.normalize(request.getPhone()) : null;

        User user = User.builder()
                .name(request.getName())
                .phone(normalizedPhone)
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
    @Transactional(readOnly = true)
    public void forgotPassword(ForgotPasswordRequest request) {
        User currentUser = requireCurrentActiveUser();
        verifyPhoneMatchesCurrentUser(currentUser, request.getPhone());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User currentUser = requireCurrentActiveUser();
        verifyPhoneMatchesCurrentUser(currentUser, request.getPhone());

        currentUser.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
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

    private User requireCurrentActiveUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = SecurityUtils.resolveUserId(auth);

        if (userId == null) {
            throw ApiException.unauthorized("Login required to change password");
        }

        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> ApiException.unauthorized("Account no longer active"));
    }

    private void verifyPhoneMatchesCurrentUser(User currentUser, String inputPhone) {
        if (currentUser.getPhone() == null || currentUser.getPhone().isBlank()) {
            throw ApiException.badRequest("No mobile number is registered for your account");
        }
        if (!phonesMatch(currentUser.getPhone(), inputPhone)) {
            throw ApiException.badRequest("Mobile number does not match your account");
        }
    }

    private boolean phonesMatch(String registeredPhone, String inputPhone) {
        if (registeredPhone == null || registeredPhone.isBlank()) {
            return false;
        }
        return PhoneUtils.normalize(registeredPhone).equals(PhoneUtils.normalize(inputPhone));
    }

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
