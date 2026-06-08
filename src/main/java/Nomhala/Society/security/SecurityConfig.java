package Nomhala.Society.security;

import Nomhala.Society.entity.Payment;
import Nomhala.Society.repository.PaymentRepository;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;


import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Autowired
    private PaymentRepository paymentRepo;

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
                                "/favicon.ico",
                                "/api/auth/**"
                        ).permitAll()

                        // ================= AUTH ENDPOINTS =================
                        .requestMatchers("/api/auth/**").permitAll()

                        // ================= ADMIN =================
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        //============VIEW FILES ==============================
                        .requestMatchers("/api/files/**")
                        .hasAnyRole("ADMIN","STAFF")

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
    @GetMapping("/payment/{id}")
    public ResponseEntity<Resource> viewPayment(
            @PathVariable Long id
    ) {
        Payment payment =
                paymentRepo.findById(id)
                        .orElseThrow();

        Path path =
                Paths.get(payment.getProofPath());


        UrlResource resource;
        try {

            resource = new UrlResource(path.toUri());

            return ResponseEntity.ok()
                    .body((Resource) resource);

        } catch (MalformedURLException e) {

            throw new RuntimeException(
                    "File not found",
                    e
            );
        }

    }
}