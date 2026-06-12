package me.code.springboot_postgres.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import me.code.springboot_postgres.models.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtil {

    private static final String CLAIM_ID = "id";
    private static final String CLAIM_ROLES = "roles";

    private final Key key;
    private final long expirationMs;

    public JwtTokenUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret must be configured via JWT_SECRET environment variable");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        String id = user.getId();
        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(id)
                .claim(CLAIM_ID, id)
                .claim(CLAIM_ROLES, roleNames)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public boolean isValidToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException jwtException) {
            return false;
        }
    }

    public String getTokenId(String token) {
        return getTokenClaim(token, "id");
    }

    @SuppressWarnings("unchecked")
    public List<String> getTokenRoles(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        Object rolesObj = claims.get(CLAIM_ROLES);
        if (rolesObj instanceof List) {
            return (List<String>) rolesObj;
        }
        return List.of();
    }

    public String getTokenClaim(String token, String type) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get(type, String.class);
    }
}
