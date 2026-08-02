package com.hireflow.security;

import com.hireflow.config.HireFlowProperties;
import com.hireflow.domain.enums.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        String privKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String pubKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        HireFlowProperties props = new HireFlowProperties();
        props.getJwt().setPrivateKey(privKeyBase64);
        props.getJwt().setPublicKey(pubKeyBase64);
        props.getJwt().setAccessTokenExpirySeconds(900);

        jwtProvider = new JwtProvider(props);
        jwtProvider.init();
    }

    @Test
    @DisplayName("Should generate and validate access token with correct claims")
    void testGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        String email = "test@hireflow.ai";
        UserRole role = UserRole.RECRUITER;

        String token = jwtProvider.generateAccessToken(userId, email, role);

        assertNotNull(token);
        assertTrue(jwtProvider.isTokenValid(token));

        Claims claims = jwtProvider.validateAndExtractClaims(token);
        assertEquals(userId, jwtProvider.extractUserId(claims));
        assertEquals(email, jwtProvider.extractEmail(claims));
        assertEquals(role, jwtProvider.extractRole(claims));
    }
}
