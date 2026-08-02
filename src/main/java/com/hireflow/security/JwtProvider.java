package com.hireflow.security;

import com.hireflow.config.HireFlowProperties;
import com.hireflow.domain.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * RS256 JWT provider — generates and validates access tokens.
 *
 * <p>Keys are loaded once at startup from Base64-encoded PEM values
 * in {@code application.yml} (sourced from env vars — no secrets in code).
 *
 * <p>Access token claims:
 * <pre>
 * {
 *   "sub":   "user-uuid",
 *   "email": "user@example.com",
 *   "role":  "RECRUITER",
 *   "iat":   1705312800,
 *   "exp":   1705313700,
 *   "jti":   "unique-jwt-id"
 * }
 * </pre>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {

    private final HireFlowProperties props;

    private RSAPrivateKey privateKey;
    private RSAPublicKey  publicKey;

    @PostConstruct
    void init() {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");

            // Decode Base64 private key (PKCS8 DER format)
            byte[] privBytes = Base64.getDecoder().decode(
                    props.getJwt().getPrivateKey().replaceAll("\\s+", ""));
            privateKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(privBytes));

            // Decode Base64 public key (X509 DER format)
            byte[] pubBytes = Base64.getDecoder().decode(
                    props.getJwt().getPublicKey().replaceAll("\\s+", ""));
            publicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(pubBytes));

            log.info("JwtProvider initialised — algorithm: RS256, " +
                    "access-token TTL: {}s", props.getJwt().getAccessTokenExpirySeconds());
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to initialise JWT RSA keys. " +
                    "Check hireflow.jwt.private-key and public-key in application.yml.", e);
        }
    }

    /**
     * Generate a signed RS256 access token for the given user.
     */
    public String generateAccessToken(UUID userId, String email, UserRole role) {
        Instant now    = Instant.now();
        Instant expiry = now.plusSeconds(props.getJwt().getAccessTokenExpirySeconds());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())         // jti — prevents token replay
                .subject(userId.toString())               // sub
                .claim("email", email)
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(privateKey)                     // RS256 inferred from RSAPrivateKey
                .compact();
    }

    /**
     * Parse and validate a JWT. Returns the claims if valid.
     *
     * @throws JwtException on invalid, expired, or tampered tokens
     */
    public Claims validateAndExtractClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Convenience helpers to extract individual claims without re-parsing.
     */
    public UUID extractUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public String extractEmail(Claims claims) {
        return claims.get("email", String.class);
    }

    public UserRole extractRole(Claims claims) {
        return UserRole.valueOf(claims.get("role", String.class));
    }

    /**
     * Silently checks token validity — returns {@code false} instead of throwing.
     * Used for logging suspicious requests without breaking the filter chain.
     */
    public boolean isTokenValid(String token) {
        try {
            validateAndExtractClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("JWT signature invalid: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
        }
        return false;
    }
}
