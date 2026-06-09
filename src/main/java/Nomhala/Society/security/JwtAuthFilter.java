package Nomhala.Society.security;

import Nomhala.Society.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException, ServletException {

        // 🔥 FIX 2: SKIP STATIC + PUBLIC RESOURCES
        if (
                request.getRequestURI().startsWith("/pages/")
                        || request.getRequestURI().startsWith("/css/")
                        || request.getRequestURI().startsWith("/js/")
                        || request.getRequestURI().startsWith("/images/")
                        || request.getRequestURI().startsWith("/api/auth/")
        ) {
            filterChain.doFilter(request, response);
            return;
        }
        // Skip auth endpoints
        if (request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            Claims claims = jwtUtil.extractAllClaims(token);

            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            if (role == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // FIX: avoid ROLE_ROLE_ duplication
            String authorityRole =
                    role.startsWith("ROLE_") ? role : "ROLE_" + role;

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority(authorityRole))
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}