package com.hireflow.service.auth;

import com.hireflow.config.HireFlowProperties;
import com.hireflow.domain.Candidate;
import com.hireflow.domain.Recruiter;
import com.hireflow.domain.RefreshToken;
import com.hireflow.domain.User;
import com.hireflow.domain.enums.UserRole;
import com.hireflow.dto.request.LoginRequest;
import com.hireflow.dto.request.RefreshTokenRequest;
import com.hireflow.dto.request.RegisterRequest;
import com.hireflow.dto.response.AuthResponse;
import com.hireflow.dto.response.UserResponse;
import com.hireflow.exception.BusinessRuleException;
import com.hireflow.exception.ConflictException;
import com.hireflow.exception.UnauthorizedException;
import com.hireflow.mapper.UserMapper;
import com.hireflow.repository.CandidateRepository;
import com.hireflow.repository.RecruiterRepository;
import com.hireflow.repository.RefreshTokenRepository;
import com.hireflow.repository.UserRepository;
import com.hireflow.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Implementation of {@link AuthService}.
 * Handles registration, authentication, refresh token rotation with family revocation, and logout.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RecruiterRepository recruiterRepository;
    private final CandidateRepository candidateRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;
    private final HireFlowProperties props;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("User with email " + request.getEmail() + " already exists");
        }

        if (request.getRole() == UserRole.ADMIN) {
            throw new BusinessRuleException("ADMIN role cannot be self-registered");
        }

        User user = userMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        // Automatically initialize profile based on role
        if (request.getRole() == UserRole.RECRUITER) {
            Recruiter recruiter = Recruiter.builder()
                    .user(savedUser)
                    .companyName("Company Pending")
                    .build();
            recruiterRepository.save(recruiter);
        } else if (request.getRole() == UserRole.CANDIDATE) {
            Candidate candidate = Candidate.builder()
                    .user(savedUser)
                    .build();
            candidateRepository.save(candidate);
        }

        log.info("User registered successfully: id={}, email={}, role={}",
                savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        RefreshToken refreshToken = createRefreshToken(user, UUID.randomUUID());

        log.info("User logged in successfully: id={}, email={}", user.getId(), user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(props.getJwt().getAccessTokenExpirySeconds())
                .user(userMapper.toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String tokenStr = request.getRefreshToken();
        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired refresh token"));

        if (refreshToken.isRevoked()) {
            // Replay attack detected! Revoke the entire token family.
            log.warn("Revoked refresh token reuse detected! Revoking token family: {}", refreshToken.getFamilyId());
            refreshTokenRepository.revokeAllByFamilyId(refreshToken.getFamilyId());
            throw new UnauthorizedException("Refresh token security violation. All sessions in family revoked.");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new UnauthorizedException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        // Rotate token: revoke current token, issue new token in same family
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        RefreshToken newRefreshToken = createRefreshToken(user, refreshToken.getFamilyId());
        String newAccessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());

        log.info("Token rotated for user: id={}", user.getId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(props.getJwt().getAccessTokenExpirySeconds())
                .user(userMapper.toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        String tokenStr = request.getRefreshToken();
        refreshTokenRepository.findByToken(tokenStr).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            log.info("User logged out, refresh token revoked: user_id={}", rt.getUser().getId());
        });
    }

    private RefreshToken createRefreshToken(User user, UUID familyId) {
        Instant expiresAt = Instant.now().plus(props.getJwt().getRefreshTokenExpiryDays(), ChronoUnit.DAYS);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .familyId(familyId)
                .isRevoked(false)
                .expiresAt(expiresAt)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }
}
