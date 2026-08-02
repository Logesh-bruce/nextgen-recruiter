package com.hireflow.security.oauth2;

import com.hireflow.domain.Candidate;
import com.hireflow.domain.User;
import com.hireflow.domain.enums.UserRole;
import com.hireflow.exception.UnauthorizedException;
import com.hireflow.repository.CandidateRepository;
import com.hireflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Custom OAuth2 user service processing OAuth2 user log-ins.
 * Maps Google OAuth2 attributes to {@link User} entities, creating candidate profiles
 * automatically for new sign-ups.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new UnauthorizedException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.isActive()) {
                throw new UnauthorizedException("Account is deactivated");
            }
            user = updateExistingUser(user, oAuth2UserInfo, registrationId);
        } else {
            user = registerNewOAuth2User(userRequest, oAuth2UserInfo);
        }

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    private User registerNewOAuth2User(OAuth2UserRequest userRequest, OAuth2UserInfo oAuth2UserInfo) {
        String provider = userRequest.getClientRegistration().getRegistrationId();

        User user = User.builder()
                .email(oAuth2UserInfo.getEmail())
                .firstName(StringUtils.hasText(oAuth2UserInfo.getFirstName()) ? oAuth2UserInfo.getFirstName() : "User")
                .lastName(StringUtils.hasText(oAuth2UserInfo.getLastName()) ? oAuth2UserInfo.getLastName() : "")
                .avatarUrl(oAuth2UserInfo.getImageUrl())
                .role(UserRole.CANDIDATE) // Default role for Google OAuth sign-up
                .oauthProvider(provider)
                .oauthSubject(oAuth2UserInfo.getId())
                .isActive(true)
                .isEmailVerified(true) // Google emails are pre-verified
                .build();

        User savedUser = userRepository.save(user);

        // Auto-create Candidate profile
        Candidate candidate = Candidate.builder()
                .user(savedUser)
                .build();
        candidateRepository.save(candidate);

        log.info("Registered new Google OAuth2 candidate user: email={}, id={}", savedUser.getEmail(), savedUser.getId());

        return savedUser;
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo, String provider) {
        boolean updated = false;

        if (existingUser.getOauthProvider() == null) {
            existingUser.setOauthProvider(provider);
            existingUser.setOauthSubject(oAuth2UserInfo.getId());
            existingUser.setEmailVerified(true);
            updated = true;
        }

        if (StringUtils.hasText(oAuth2UserInfo.getImageUrl()) && !oAuth2UserInfo.getImageUrl().equals(existingUser.getAvatarUrl())) {
            existingUser.setAvatarUrl(oAuth2UserInfo.getImageUrl());
            updated = true;
        }

        return updated ? userRepository.save(existingUser) : existingUser;
    }
}
