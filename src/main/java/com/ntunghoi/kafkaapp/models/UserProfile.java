package com.ntunghoi.kafkaapp.models;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Schema(description = "User profile")
public interface UserProfile {
    int getId();
    String getName();
    String getEmail();
    String getUsername();
    String getPassword();
    boolean isAccountNonExpired();
    boolean isCredentialsNonExpired();
    boolean isEnabled();
    String getPreferredCurrency();
    Collection<? extends GrantedAuthority> getAuthorities();
}
