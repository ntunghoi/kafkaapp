package com.ntunghoi.kafkaapp.controllers.authentication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request details for authentication")
public class AuthenticationRequest {
    @Schema(
            description = "Email used for authentication",
            example = "user1@email.com"
    )
    @Email(message = "Must be a valid email address")
    private String email;

    @Schema(
            description = "Password for authentication",
            example = "password123"
    )
    @NotBlank(message = "Value cannot be blank")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


