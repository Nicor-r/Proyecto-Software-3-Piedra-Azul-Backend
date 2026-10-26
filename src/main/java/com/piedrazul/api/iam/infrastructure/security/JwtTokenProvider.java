package com.piedrazul.api.iam.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.provider.TokenProvider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider implements TokenProvider {

    private static final long EXPIRATION_MILLIS = 3600000L;

    private final SecretKey secretKey;
    private final KeycloakKeyResolver keycloakKeyResolver;

    @Autowired
    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret,
                            KeycloakKeyResolver keycloakKeyResolver) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.keycloakKeyResolver = keycloakKeyResolver;
    }

    public JwtTokenProvider(String jwtSecret) {
        this(jwtSecret, new KeycloakKeyResolver(""));
    }

    @Override
    public String generateToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_MILLIS);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String getEmailFromToken(String token) {
        Claims claims = parseClaims(token);
        String email = claims.get("email", String.class);
        if (email != null && !email.isBlank()) {
            return email;
        }
        String preferredUsername = claims.get("preferred_username", String.class);
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }
        return claims.getSubject();
    }

    @Override
    public RoleEnum getRoleFromToken(String token) {
        Claims claims = parseClaims(token);

        // 1. Reclamación directa "role" (formato local HMAC)
        String directRole = claims.get("role", String.class);
        if (directRole != null && !directRole.isBlank()) {
            try {
                return RoleEnum.valueOf(directRole.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        // 2. Roles de Keycloak a nivel de Realm ("realm_access.roles")
        Object realmAccessObj = claims.get("realm_access");
        if (realmAccessObj instanceof Map<?, ?> realmAccess) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof List<?> roles) {
                RoleEnum detected = matchRoleFromList(roles);
                if (detected != null) {
                    return detected;
                }
            }
        }

        // 3. Roles de Keycloak a nivel de Resource / Client ("resource_access.*.roles")
        Object resourceAccessObj = claims.get("resource_access");
        if (resourceAccessObj instanceof Map<?, ?> resourceAccess) {
            for (Object clientObj : resourceAccess.values()) {
                if (clientObj instanceof Map<?, ?> clientMap) {
                    Object rolesObj = clientMap.get("roles");
                    if (rolesObj instanceof List<?> roles) {
                        RoleEnum detected = matchRoleFromList(roles);
                        if (detected != null) {
                            return detected;
                        }
                    }
                }
            }
        }

        return null;
    }

    private RoleEnum matchRoleFromList(List<?> roles) {
        if (roles.contains("ADMIN") || roles.contains("admin")) {
            return RoleEnum.ADMIN;
        }
        if (roles.contains("AGENDADOR") || roles.contains("agendador")) {
            return RoleEnum.AGENDADOR;
        }
        if (roles.contains("PATIENT") || roles.contains("patient") || roles.contains("PACIENTE") || roles.contains("paciente")) {
            return RoleEnum.PATIENT;
        }
        return null;
    }

    private Claims parseClaims(String token) {
        Locator<Key> keyLocator = header -> {
            String alg = header.getAlgorithm();
            if (alg != null && alg.startsWith("HS")) {
                return secretKey;
            }
            if (alg != null && alg.startsWith("RS")) {
                String kid = (String) header.get("kid");
                return keycloakKeyResolver.resolveKey(kid);
            }
            return secretKey;
        };

        return Jwts.parser()
                .keyLocator(keyLocator)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
