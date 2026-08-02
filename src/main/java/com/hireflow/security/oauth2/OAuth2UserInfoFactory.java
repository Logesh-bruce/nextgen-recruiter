package com.hireflow.security.oauth2;

import com.hireflow.exception.UnauthorizedException;

import java.util.Map;

/**
 * Factory for creating provider-specific {@link OAuth2UserInfo} instances.
 */
public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return new GoogleOAuth2UserInfo(attributes);
        } else {
            throw new UnauthorizedException("Login with " + registrationId + " is not supported.");
        }
    }
}
