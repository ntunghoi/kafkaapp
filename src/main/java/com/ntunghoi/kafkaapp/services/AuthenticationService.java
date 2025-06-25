package com.ntunghoi.kafkaapp.services;

import com.ntunghoi.kafkaapp.entities.UserProfileEntity;
import com.ntunghoi.kafkaapp.exceptions.SystemConfigurationException;
import com.ntunghoi.kafkaapp.models.UserProfile;
import com.ntunghoi.kafkaapp.repositories.SystemDataRepository;
import com.ntunghoi.kafkaapp.repositories.UserProfilesRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthenticationService {
    private final UserProfilesRepository userProfilesRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SystemDataRepository systemDateRepository;

    public record LoginRequest(String username, String password) {
    }

    public record SignUpRequest(
            String username,
            String password,
            String name,
            String preferredCurrency
    ) {
    }

    public AuthenticationService(
            UserProfilesRepository userProfilesRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            SystemDataRepository systemDateRepository
    ) {
        this.userProfilesRepository = userProfilesRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.systemDateRepository = systemDateRepository;
    }

    public UserProfile signUp(SignUpRequest signUpRequest) throws Exception {
        UserProfileEntity userProfileEntity = new UserProfileEntity()
                .setUsername(signUpRequest.username)
                .setPassword(passwordEncoder.encode(signUpRequest.password))
                .setName(signUpRequest.name)
                .setPreferredCurrency(signUpRequest.preferredCurrency)
                .setRoles(Set.of(
                        systemDateRepository.findByCode("client")
                                .orElseThrow(() -> new SystemConfigurationException("Unknow role [client]"))
                ));


        return userProfilesRepository.save(userProfileEntity);
    }

    public UserProfile authenticate(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username,
                        loginRequest.password
                )
        );
        return userProfilesRepository.findByEmail(loginRequest.username).orElseThrow();
    }
}
