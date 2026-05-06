package com.steve.secure_auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration request")
public class RegisterRequest {

    @Schema(description = "Username", example = "mysteve")
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(description = "Email address", example = "steve@example.com")
    @NotBlank
    @Email(message = "Valid email is required")
    private String email;

    @Schema(description = "Password - minimum 8 characters", example = "SecurePass123!")
    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Schema(description = "First name", example = "Steve")
    @NotBlank(message = "First name is required")
    private String firstName;

    @Schema(description = "Last name", example = "Jobs")
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Schema(description = "Profile image file (optional)", type = "string", format = "binary")
    private MultipartFile profileImage;
}