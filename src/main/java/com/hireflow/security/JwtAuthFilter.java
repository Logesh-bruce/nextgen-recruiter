package com.hireflow.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Intercepts every HTTP request and validates the JWT Bearer token.
 *
 * <p>Flow:
 * <ol>
 *   <li>Extract {@code Authorization: Bearer <token>} header</li>
 *   <li>Validate token with {@link JwtProvider}</li>
 *   <li>Load {@link SecurityUser} from database</li>
 *   <li>Set {@link org.springframework.security.core.Authentication} in
 *       {@link SecurityContextHolder}</li>
 *   <li>Inject {@code traceId} into {@link MDC} for structured logging</li>
 * </ol>
 *
 * <p>Missing or invalid tokens are silently ignored here — Spring Security's
 * access rules will then reject the request with 401/403 if the endpoint
 * requires authentication.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_HEADER   = "Authorization";
    private static final String TRACE_ID_KEY  = "traceId";

    private final JwtProvider              jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Inject a trace ID into every request for log correlation
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID_KEY, traceId);
        response.setHeader("X-Trace-Id", traceId);

        try {
            String token = extractToken(request);

            if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                processToken(token, request);
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_KEY);   // Always clean up MDC to prevent thread-local leaks
        }
    }

    private void processToken(String token, HttpServletRequest request) {
        try {
            Claims claims = jwtProvider.validateAndExtractClaims(token);
            String email  = jwtProvider.extractEmail(claims);

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (userDetails.isEnabled()) {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (JwtException e) {
            // Log and continue — Spring Security will reject the request if auth is required
            log.debug("JWT validation failed for request [{} {}]: {}",
                    request.getMethod(), request.getRequestURI(), e.getMessage());
        } catch (Exception e) {
            log.warn("Unexpected error during JWT processing: {}", e.getMessage());
        }
    }

    /**
     * Extracts the raw JWT from the {@code Authorization} header.
     * Returns {@code null} if the header is absent or not a Bearer token.
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
