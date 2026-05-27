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

    // ================= SECRET KEY =================
    private final Key secretKey =
            Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // ================= GENERATE TOKEN =================
    public String generateToken(
            String email,
            String role
    ) {

        return Jwts.builder()

                .setSubject(email)

                .claim("role", role)

                .setIssuedAt(new Date())

                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + 1000 * 60 * 60 * 24
                        )
                )

                .signWith(secretKey)

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

    // ================= EXTRACT EMAIL =================
    public String extractEmail(String token) {

        return extractAllClaims(token)
                .getSubject();
    }

    // ================= EXTRACT ROLE =================
    public String extractRole(String token) {

        return extractAllClaims(token)
                .get("role", String.class);
    }

    // ================= VALIDATE TOKEN =================
    public boolean validateToken(String token) {

        try {

            extractAllClaims(token);

            return true;

        }

        catch (Exception e) {

            return false;
        }
    }
}