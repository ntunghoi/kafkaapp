package com.ntunghoi.kafkaapp.configurations;

import com.ntunghoi.kafkaapp.components.AuthEntryPointJwt;
import com.ntunghoi.kafkaapp.components.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder;

    private final AuthEntryPointJwt unauthorizedHandler;

    @Value("${springdoc.swagger-ui.path}")
    private String swaggerUiPath;

    @Value("${springdoc.api-docs.path}")
    private String apiDocsPath;

    @Value("${spring.h2.console.enabled}")
    private boolean ish2ConsoleEnabled;

    public SecurityConfiguration(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            UserDetailsService userDetailsService,
            BCryptPasswordEncoder passwordEncoder,
            AuthEntryPointJwt unauthorizedHandler
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.unauthorizedHandler = unauthorizedHandler;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String swaggerUIPathPattern = String.format("%s/**", swaggerUiPath.split("\\.")[0]);
        String apiDocsPathPattern = String.format("%s/**", apiDocsPath);
        logger.info("Allow access to API document: {} / {}", swaggerUIPathPattern, apiDocsPathPattern);

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        http
                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .authorizeHttpRequests(authorize -> {
                            if(ish2ConsoleEnabled) {
                                authorize.requestMatchers(toH2Console()).permitAll();
                            }
                            authorize
                                    .requestMatchers("/actuator/**").permitAll()
                                    .requestMatchers("/auth/**").permitAll()
                                    .requestMatchers(swaggerUiPath).permitAll()
                                    .requestMatchers(swaggerUIPathPattern).permitAll()
                                    .requestMatchers(apiDocsPathPattern).permitAll()
                                    .anyRequest()
                                    .authenticated();
                        }
                )
                .exceptionHandling(exception -> {
                    exception.authenticationEntryPoint(unauthorizedHandler);
                })
                .securityContext(context -> context.requireExplicitSave(false)) // required for Spring Boot 3.x
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider)
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:8080"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }

}
