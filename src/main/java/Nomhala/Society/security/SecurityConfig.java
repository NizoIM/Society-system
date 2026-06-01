package Nomhala.Society.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;


import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // DISABLE CSRF
                .csrf(csrf -> csrf.disable())

                // ENABLE CORS
                .cors(cors -> {})

                // JWT = STATELESS
                .sessionManagement(session ->

                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                .authorizeHttpRequests(auth -> auth

                        // ================= PUBLIC STATIC FILES =================
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/pages/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()

                        // ================= AUTH ENDPOINTS =================
                        .requestMatchers("/api/auth/**").permitAll()

                        // ================= ADMIN =================
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ================= STAFF =================
                        .requestMatchers("/api/staff/**").hasAnyRole("STAFF", "ADMIN")

                        // ================= MEMBER =================
                        .requestMatchers("/api/member/**").hasAnyRole("MEMBER", "STAFF", "ADMIN")

                        // ================= ERROR HANDLING =================
                        .requestMatchers("/error").permitAll()

                        // EVERYTHING ELSE
                        .anyRequest().authenticated()
                )

                // JWT FILTER
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter
    ) {

        this.jwtAuthFilter =
                jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}