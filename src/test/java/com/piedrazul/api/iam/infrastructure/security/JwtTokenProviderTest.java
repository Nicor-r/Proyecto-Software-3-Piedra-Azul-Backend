package com.piedrazul.api.iam.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;

import io.jsonwebtoken.Jwts;

class JwtTokenProviderTest {

    private KeycloakKeyResolver keycloakKeyResolver;
    private JwtTokenProvider tokenProvider;
    private KeyPair rsaKeyPair;

    @BeforeEach
    void setUp() throws Exception {
        keycloakKeyResolver = new KeycloakKeyResolver("http://dummy-keycloak-url");
        tokenProvider = new JwtTokenProvider("01234567890123456789012345678901", keycloakKeyResolver);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        rsaKeyPair = kpg.generateKeyPair();
        keycloakKeyResolver.registerKey("keycloak-key-1", rsaKeyPair.getPublic());
    }

    @Test
    void generateToken_incluyeRolAdminVerificable() {
        User admin = new User("admin-id", "Administrador", "admin@test.com", "ADMIN",
                "0000000000", "hashed-password", RoleEnum.ADMIN);

        String token = tokenProvider.generateToken(admin);

        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.getEmailFromToken(token)).isEqualTo("admin@test.com");
        assertThat(tokenProvider.getRoleFromToken(token)).isEqualTo(RoleEnum.ADMIN);
    }

    @Test
    void validateToken_conTokenKeycloakAdmin_esValidoYExtraeRol() {
        Date now = new Date();
        Date exp = new Date(now.getTime() + 3600000);

        String keycloakToken = Jwts.builder()
                .header().keyId("keycloak-key-1").and()
                .subject("keycloak-admin-uuid")
                .claim("email", "admin@piedrazul.com")
                .claim("realm_access", Map.of("roles", List.of("offline_access", "ADMIN")))
                .issuedAt(now)
                .expiration(exp)
                .signWith(rsaKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        assertThat(tokenProvider.validateToken(keycloakToken)).isTrue();
        assertThat(tokenProvider.getEmailFromToken(keycloakToken)).isEqualTo("admin@piedrazul.com");
        assertThat(tokenProvider.getRoleFromToken(keycloakToken)).isEqualTo(RoleEnum.ADMIN);
    }

    @Test
    void validateToken_conTokenKeycloakAgendador_extraeRolAgendador() {
        Date now = new Date();
        Date exp = new Date(now.getTime() + 3600000);

        String keycloakToken = Jwts.builder()
                .header().keyId("keycloak-key-1").and()
                .subject("keycloak-agendador-uuid")
                .claim("preferred_username", "luisa@piedrazul.com")
                .claim("realm_access", Map.of("roles", List.of("AGENDADOR")))
                .issuedAt(now)
                .expiration(exp)
                .signWith(rsaKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        assertThat(tokenProvider.validateToken(keycloakToken)).isTrue();
        assertThat(tokenProvider.getEmailFromToken(keycloakToken)).isEqualTo("luisa@piedrazul.com");
        assertThat(tokenProvider.getRoleFromToken(keycloakToken)).isEqualTo(RoleEnum.AGENDADOR);
    }

    @Test
    void validateToken_conTokenKeycloakPatient_extraeRolPatient() {
        Date now = new Date();
        Date exp = new Date(now.getTime() + 3600000);

        String keycloakToken = Jwts.builder()
                .header().keyId("keycloak-key-1").and()
                .subject("keycloak-paciente-uuid")
                .claim("email", "paciente@gmail.com")
                .claim("resource_access", Map.of("piedrazul-api", Map.of("roles", List.of("PATIENT"))))
                .issuedAt(now)
                .expiration(exp)
                .signWith(rsaKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        assertThat(tokenProvider.validateToken(keycloakToken)).isTrue();
        assertThat(tokenProvider.getEmailFromToken(keycloakToken)).isEqualTo("paciente@gmail.com");
        assertThat(tokenProvider.getRoleFromToken(keycloakToken)).isEqualTo(RoleEnum.PATIENT);
    }

    @Test
    void validateToken_conTokenExpirado_retornaFalse() {
        Date now = new Date();
        Date pasada = new Date(now.getTime() - 10000);

        String expiredToken = Jwts.builder()
                .header().keyId("keycloak-key-1").and()
                .subject("usuario")
                .claim("email", "test@test.com")
                .issuedAt(new Date(now.getTime() - 20000))
                .expiration(pasada)
                .signWith(rsaKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        assertThat(tokenProvider.validateToken(expiredToken)).isFalse();
    }

    @Test
    void validateToken_conTokenInvalido_retornaFalse() {
        assertThat(tokenProvider.validateToken("token.invalido.malformado")).isFalse();
    }
}
