package com.ntunghoi.kafkaapp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntunghoi.kafkaapp.components.AuthEntryPointJwt;
import com.ntunghoi.kafkaapp.configurations.ApplicationConfiguration;
import com.ntunghoi.kafkaapp.configurations.SecurityConfiguration;
import com.ntunghoi.kafkaapp.controllers.authentication.AuthenticationController;
import com.ntunghoi.kafkaapp.entities.RoleEntity;
import com.ntunghoi.kafkaapp.entities.UserProfileEntity;
import com.ntunghoi.kafkaapp.repositories.SystemDataRepository;
import com.ntunghoi.kafkaapp.repositories.UserProfilesRepository;
import com.ntunghoi.kafkaapp.services.AuthenticationService;
import com.ntunghoi.kafkaapp.services.JwtService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@Import({
        ApplicationConfiguration.class,
        AuthEntryPointJwt.class,
        AuthenticationService.class,
        SecurityConfiguration.class,
        SystemDataRepository.class
})
public class AuthenticationControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserProfilesRepository userProfilesRepository;

    @MockitoBean
    private SystemDataRepository systemDataRepository;

    @MockitoBean
    private RoleEntity roleEntity;

    @MockitoBean
    private UserProfileEntity userProfileEntity;


    private String toPostBody(Map<String, String> entries) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(entries);
    }

    @Nested
    class SignUp {
        private MockHttpServletRequestBuilder postRequestBuilder() {
            return post("/auth/registrations");
        }

        @Nested
        class ValidParameters {
            @Test
            void test_AuthenticationController_SignUp_Success() throws Exception {
                when(systemDataRepository.findByCode("client")).thenReturn(Optional.of(roleEntity));

                String email = "email1@email.com";
                when(userProfileEntity.getId()).thenReturn(0);
                when(userProfileEntity.getName()).thenReturn("user1");
                when(userProfileEntity.getEmail()).thenReturn(email);
                when(userProfileEntity.getUsername()).thenReturn(email);
                when(userProfileEntity.getPassword()).thenReturn("password");
                when(userProfileEntity.getPreferredCurrency()).thenReturn("EUR");
                when(userProfileEntity.isEnabled()).thenReturn(true);
                when(userProfileEntity.isCredentialsNonExpired()).thenReturn(true);
                when(userProfileEntity.isAccountNonLocked()).thenReturn(true);
                when(userProfilesRepository.save(any())).thenReturn(userProfileEntity);

                mockMvc.perform(
                                postRequestBuilder()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toPostBody(
                                                        Map.of(
                                                                "email", userProfileEntity.getEmail(),
                                                                "name", userProfileEntity.getName(),
                                                                "password", userProfileEntity.getPassword(),
                                                                "confirm_password", userProfileEntity.getPassword()
                                                        )
                                                )
                                        )
                        ).andExpect(status().isOk())
                        .andExpect(jsonPath("id").value(userProfileEntity.getId()))
                        .andExpect(jsonPath("name").value(userProfileEntity.getName()))
                        .andExpect(jsonPath("password").value(userProfileEntity.getPassword()))
                        .andExpect(jsonPath("preferredCurrency").value(userProfileEntity.getPreferredCurrency()))
                        .andExpect(jsonPath("email").value(userProfileEntity.getEmail()))
                        .andExpect(jsonPath("username").value(userProfileEntity.getUsername()))
                        .andExpect(jsonPath("enabled").value(userProfileEntity.isEnabled()))
                        .andExpect(jsonPath("accountNonLocked").value(userProfileEntity.isAccountNonLocked()))
                        .andExpect(jsonPath("credentialsNonExpired").value(userProfileEntity.isCredentialsNonExpired()));
                verify(userProfilesRepository, times(1)).save(any());
            }
        }

        @Nested
        class InvalidParameters {
            @Test
            void test_AuthenticationController_SignUp_InvalidParameters_MissingPostBody() throws Exception {
                mockMvc.perform(postRequestBuilder())
                        .andExpect(status().isBadRequest())
                        .andExpect(
                                content().string("Missing request body for the required data")
                        );
            }

            @Test
            void test_AuthenticationController_SignUp_InvalidParameters_MissingParameter() throws Exception {
                mockMvc.perform(
                                postRequestBuilder()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toPostBody(
                                                        Map.of(
                                                                "email", "",
                                                                "name", "",
                                                                "password", "",
                                                                "confirm_password", ""
                                                        )
                                                )
                                        )
                        ).andExpect(status().isBadRequest())
                        .andExpect(jsonPath("name").value("Value cannot be blank"))
                        .andExpect(jsonPath("email").value("Value cannot be blank"))
                        .andExpect(jsonPath("password").value("Value cannot be blank"))
                        .andExpect(jsonPath("confirm_password").value("Value cannot be blank"));
            }

            @Test
            void test_AuthenticationController_SignUp_InvalidParameters_UnmatchedConfirmPassword() throws Exception {
                mockMvc.perform(
                                postRequestBuilder()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toPostBody(
                                                        Map.of(
                                                                "email", "user1@email.com",
                                                                "name", "user1",
                                                                "password", "password",
                                                                "confirm_password", "not_password"
                                                        )
                                                )
                                        )
                        ).andExpect(status().isBadRequest())
                        .andExpect(
                                content().string("Input is not valid")
                        );
            }

            @Test
            void test_AuthenticationController_SignUp_InvalidParameters_InvalidEmail() throws Exception {
                mockMvc.perform(
                                postRequestBuilder()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toPostBody(
                                                        Map.of(
                                                                "email", "user1",
                                                                "name", "user1",
                                                                "password", "password",
                                                                "confirm_password", "password"
                                                        )
                                                )
                                        )
                        ).andExpect(status().isBadRequest())
                        .andExpect(jsonPath("email").value( "Must be a valid email address"));
            }
        }
    }

    @Nested
    class SignIn {
        private MockHttpServletRequestBuilder postRequestBuilder() {
            return post("/auth/sessions");
        }

        @Nested
        class ValidParameters {
            @Test
            void test_AuthenticationController_Sessions_Success() throws Exception {
                when(userProfileEntity.getId()).thenReturn(0);
                when(userProfileEntity.getName()).thenReturn("user1");
                when(userProfileEntity.getEmail()).thenReturn("email1@email.com");
                when(userProfileEntity.getPassword()).thenReturn(new BCryptPasswordEncoder().encode("password"));
                when(userProfileEntity.isAccountNonLocked()).thenReturn(true);
                when(userProfileEntity.isEnabled()).thenReturn(true);
                when(userProfileEntity.isAccountNonExpired()).thenReturn(true);
                when(userProfileEntity.isCredentialsNonExpired()).thenReturn(true);
                when(userProfilesRepository.findByEmail(any())).thenReturn(Optional.of(userProfileEntity));

                String jwtToken = "fake_jwt_token";
                long expireInSeconds = 1000L;
                when(jwtService.generateToken(any())).thenReturn(jwtToken);
                when(jwtService.getExpireInSeconds()).thenReturn(expireInSeconds);

                mockMvc.perform(
                                postRequestBuilder()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                toPostBody(
                                                        Map.of(
                                                                "email", "user1@email.com",
                                                                "password", "password"
                                                        )
                                                )
                                        )
                        ).andExpect(status().isOk())
                        .andExpect(jsonPath("jwtToken").value(jwtToken))
                        .andExpect(jsonPath("expireInSeconds").value(expireInSeconds));
                verify(userProfilesRepository, times(2)).findByEmail(any());
            }
        }

        @Nested
        class InvalidParameters {
            @Test
            void test_AuthenticationController_SignIn_InvalidParameters_InvalidEmail() throws Exception {
                mockMvc.perform(
                                postRequestBuilder()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                toPostBody(
                                                        Map.of(
                                                                "email", "user1",
                                                                "password", "password"
                                                        )
                                                )
                                        )
                        ).andExpect(status().isBadRequest())
                        .andExpect(
                                jsonPath("email").value("Must be a valid email address")
                        );
            }

            @Test
            void test_AuthenticationController_SignIn_InvalidParameters_InvalidPassword() throws Exception {
                when(userProfilesRepository.findByEmail(any())).thenReturn(Optional.empty());
                mockMvc.perform(
                                postRequestBuilder()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                toPostBody(
                                                        Map.of(
                                                                "email", "user1@email.com",
                                                                "password", "password"
                                                        )
                                                )
                                        )
                        ).andExpect(status().isUnauthorized())
                        .andExpect(
                                content().string("")
                        );
            }
        }
    }
}
