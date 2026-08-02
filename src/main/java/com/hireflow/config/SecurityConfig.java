package com.hireflow.config;

import com.hireflow.security.CustomUserDetailsService;
import com.hireflow.security.JwtAuthFilter;
import com.hireflow.security.oauth2.CustomOAuth2UserService;
import com.hireflow.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.hireflow.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration.
 *
 * <p>Key decisions:
 * <ul>
 *   <li>Stateless sessions — JWT is the only auth mechanism for REST endpoints</li>
 *   <li>CSRF disabled — safe for stateless APIs (no cookies for auth)</li>
 *   <li>BCrypt cost=12 — deliberate slowness to resist brute-force</li>
 *   <li>RS256 JWT — asymmetric signing; private key stays server-side</li>
 *   <li>{@code @EnableMethodSecurity} — enables {@code @PreAuthorize} on service/controller methods</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter                     jwtAuthFilter;
    private final CustomUserDetailsService          userDetailsService;
    private final CorsConfigurationSource           corsConfigurationSource;
    private final CustomOAuth2UserService           customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    // ── Public paths (no JWT required) ────────────────────────────────────
    private static final String[] PUBLIC_POST = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh"
    };

    private static final String[] PUBLIC_GET = {
            "/api/v1/jobs",
            "/api/v1/jobs/{id}",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/health",
            "/actuator/health/**"
    };

    private static final String[] OAUTH2_PATHS = {
            "/login/oauth2/**",
            "/oauth2/**",
            "/api/v1/auth/google/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── CSRF — disabled for stateless REST (safe: no cookie-based auth) ──
            .csrf(AbstractHttpConfigurer::disable)

            // ── CORS — delegate to CorsConfig bean ────────────────────────────────
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // ── Session — stateless; no HttpSession created ───────────────────────
            .sessionManagement(sm ->
                    sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── Authorization rules ────────────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.POST, PUBLIC_POST).permitAll()
                    .requestMatchers(HttpMethod.GET,  PUBLIC_GET).permitAll()
                    .requestMatchers(OAUTH2_PATHS).permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").authenticated()
                    // Admin-only paths
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    // Everything else requires authentication
                    .anyRequest().authenticated()
            )

            // ── Authentication provider ───────────────────────────────────────────
            .authenticationProvider(authenticationProvider())

            // ── JWT filter runs before Spring's UsernamePasswordAuthenticationFilter ─
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            // ── OAuth2 login (Google) ─────────────────────────────────────────────
            .oauth2Login(oauth2 -> oauth2
                    .authorizationEndpoint(authorization -> authorization
                            .baseUri("/api/v1/auth/google"))
                    .redirectionEndpoint(redirection -> redirection
                            .baseUri("/login/oauth2/code/*"))
                    .userInfoEndpoint(userInfo -> userInfo
                            .userService(customOAuth2UserService))
                    .successHandler(oAuth2AuthenticationSuccessHandler)
                    .failureHandler(oAuth2AuthenticationFailureHandler));

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt with cost factor 12 — deliberately slow to resist offline brute-force.
     * Adjust cost upward if hardware allows (target: ~250ms per hash).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
