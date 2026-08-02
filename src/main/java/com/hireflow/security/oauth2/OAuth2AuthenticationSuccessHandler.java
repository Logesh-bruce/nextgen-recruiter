package com.hireflow.security.oauth2;

import com.hireflow.config.HireFlowProperties;
import com.hireflow.domain.RefreshToken;
import com.hireflow.domain.User;
import com.hireflow.repository.RefreshTokenRepository;
import com.hireflow.security.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Handles successful OAuth2 authentication by issuing HireFlow JWT access + refresh tokens,
 * then redirecting to the frontend application callback endpoint.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final HireFlowProperties props;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        RefreshToken refreshToken = createRefreshToken(user);

        String targetUrl = UriComponentsBuilder.fromUriString(props.getCors().getAllowedOrigins().split(",")[0] + "/oauth2/redirect")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken.getToken())
                .queryParam("expiresIn", props.getJwt().getAccessTokenExpirySeconds())
                .build().toUriString();

        log.info("OAuth2 login successful for user: {}. Redirecting to frontend callback.", user.getEmail());

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private RefreshToken createRefreshToken(User user) {
        Instant expiresAt = Instant.now().plus(props.getJwt().getRefreshTokenExpiryDays(), ChronoUnit.DAYS);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .familyId(UUID.randomUUID())
                .isRevoked(false)
                .expiresAt(expiresAt)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }
}
