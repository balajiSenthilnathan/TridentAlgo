package com.trident.trident_algo.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true) // Enable role-based annotations
public class AuthSecurityConfiguration {

    /**
     * Define which paths are publicly accessible and which require authentication.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        // Disable CSRF for APIs (stateless JWT)
                        .ignoringRequestMatchers("/api/auth/**", "/public/**")
                        .disable()
                )
                .cors(cors -> cors
                        .configurationSource(request -> {
                            CorsConfiguration config = new CorsConfiguration();
                            config.setAllowedOrigins(List.of("http://localhost:4200")); // Add your frontend domain
                            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
                            config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
                            config.setExposedHeaders(List.of("Authorization"));
                            config.setAllowCredentials(true);
                            return config;
                        }))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/**", "/public/**").permitAll()
                        // Role-specific endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasRole("USER")
                        .requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")
                        // Other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }

    /**
     * Define WebSecurityCustomizer to ignore static resources.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/swagger-ui/**", "/v3/api-docs/**");
    }
}