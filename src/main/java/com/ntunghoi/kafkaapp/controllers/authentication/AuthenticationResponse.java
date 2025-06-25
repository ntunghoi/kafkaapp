package com.ntunghoi.kafkaapp.controllers.authentication;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response for successfully authentication")
public class AuthenticationResponse {
    @Schema(
            description = "JWT Token",
            example = ""
    )
    private String jwtToken;

    @Schema(
            description = "JWT token expires in number of seconds",
            example = ""
    )
    private long expireInSeconds;

    public String getJwtToken()  {
        return jwtToken;
    }

    public AuthenticationResponse setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;

        return this;
    }

    public long getExpireInSeconds() {
        return expireInSeconds;
    }

    public AuthenticationResponse setExpirationTime(long expireInSeconds) {
        this.expireInSeconds = expireInSeconds;

        return this;
    }
}
