package com.hireflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for {@code POST /api/v1/auth/refresh} and {@code POST /api/v1/auth/logout}.
 */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token must not be blank")
    private String refreshToken;
}
