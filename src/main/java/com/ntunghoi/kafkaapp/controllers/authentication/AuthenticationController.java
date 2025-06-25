package com.ntunghoi.kafkaapp.controllers.authentication;


import com.ntunghoi.kafkaapp.models.UserProfile;
import com.ntunghoi.kafkaapp.services.AuthenticationService;
import com.ntunghoi.kafkaapp.services.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

import static com.ntunghoi.kafkaapp.configurations.OpenApiConfiguration.AUTHENTICATION_TAG;

@RestController
@RequestMapping("/auth")
@Tag(name = AUTHENTICATION_TAG)
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    public AuthenticationController(
            AuthenticationService authenticationService,
            JwtService jwtService
    ) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }

    @PostMapping("/registrations")
    @Operation(summary = "Registration")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Registration completed",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserProfile.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    public ResponseEntity<?> register(@RequestBody @Valid RegistrationRequest registrationRequest) {
        if (!Objects.equals(registrationRequest.getConfirmPassword(), registrationRequest.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Input is not valid");
        }

        try {
            return ResponseEntity.ok(authenticationService.signUp(
                    new AuthenticationService.SignUpRequest(
                            registrationRequest.getEmail(),
                            registrationRequest.getPassword(),
                            registrationRequest.getName(),
                            registrationRequest.getPreferredCurrency()
                    )
            ));
        } catch(Exception exception) {
            ResponseEntity.internalServerError().body(exception.getMessage());
        }

        return null;
    }

    @PostMapping("/sessions")
    @Operation(summary = "Log in")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Log in successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AuthenticationResponse.class)
                            )
                    }
            )
    })
    public ResponseEntity<?> authenticate(
            @RequestBody @Valid AuthenticationRequest authenticationRequest
    ) {
        UserProfile userProfile = authenticationService.authenticate(
                new AuthenticationService.LoginRequest(
                        authenticationRequest.getEmail(),
                        authenticationRequest.getPassword()
                )
        );

        String jwtToken = jwtService.generateToken(userProfile);
        return ResponseEntity.ok(
                new AuthenticationResponse()
                        .setJwtToken(jwtToken)
                        .setExpirationTime(jwtService.getExpireInSeconds())
        );
    }

}
