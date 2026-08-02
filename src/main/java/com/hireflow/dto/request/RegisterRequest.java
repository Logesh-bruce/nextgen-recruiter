package com.hireflow.dto.request;

import com.hireflow.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for {@code POST /api/v1/auth/register}.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @NotBlank(message = "First name must not be blank")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name must not be blank")
    @Size(max = 100)
    private String lastName;

    @NotNull(message = "Role is required")
    private UserRole role;  // CANDIDATE or RECRUITER only (ADMIN cannot self-register)
}
