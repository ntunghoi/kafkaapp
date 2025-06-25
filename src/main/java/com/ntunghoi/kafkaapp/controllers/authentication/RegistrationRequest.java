package com.ntunghoi.kafkaapp.controllers.authentication;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "Request details for registration")
public class RegistrationRequest {
    @Schema(
            description = "Email used to register",
            example = "user1@email.com"
    )
    @NotBlank(message = "Value cannot be blank")
    @Email(message = "Must be a valid email address")
    private String email;

    @Schema(
            description = "Password for registration",
            example = "password123"
    )
    @NotBlank(message = "Value cannot be blank")
    private String password;

    @Schema(
            description = "Confirmed password for registration",
            example = "password123"
    )
    @JsonProperty("confirm_password")
    @NotBlank(message = "Value cannot be blank")
    private String confirmPassword;

    @Schema(
            description = "Name of the user",
            example = "Peter Parker"
    )
    @NotBlank(message = "Value cannot be blank")
    private String name;

    @Schema(
            description = "Preferred currency",
            example = "USD"
    )
    private String preferredCurrency;

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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreferredCurrency() {
        return preferredCurrency;
    }

    public void setPreferredCurrency(String preferredCurrency) {
        this.preferredCurrency = preferredCurrency;
    }
}
