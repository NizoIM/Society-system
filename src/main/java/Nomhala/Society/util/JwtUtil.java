package Nomhala.Society.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // 🔥 BETTER: at least 32+ characters (DO NOT CHANGE AFTER DEPLOY)
    private final Key secretKey =
            Keys.hmacShaKeyFor(
                    "THIS_IS_A_SUPER_SECURE_SECRET_KEY_FOR_JWT_SIGNING_123456"
                            .getBytes()
            );

    // ================= GENERATE TOKEN =================
    public String generateToken(String email, String role) {

        // FIX: ensure consistent format
        String normalizedRole =
                role.startsWith("ROLE_") ? role : "ROLE_" + role;

        return Jwts.builder()
                .setSubject(email)
                .claim("role", normalizedRole)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 86400000)
                )
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // ================= EXTRACT CLAIMS =================
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ================= EMAIL =================
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ================= ROLE =================
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // ================= VALIDATE =================
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}