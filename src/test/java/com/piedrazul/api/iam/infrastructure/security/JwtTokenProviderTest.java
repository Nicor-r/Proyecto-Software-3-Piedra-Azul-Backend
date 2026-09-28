package com.piedrazul.api.iam.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;

class JwtTokenProviderTest {

    @Test
    void generateToken_incluyeRolAdminVerificable() {
        JwtTokenProvider tokenProvider = new JwtTokenProvider("01234567890123456789012345678901");
        User admin = new User("admin-id", "Administrador", "admin@test.com", "ADMIN",
                "0000000000", "hashed-password", RoleEnum.ADMIN);

        String token = tokenProvider.generateToken(admin);

        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.getEmailFromToken(token)).isEqualTo("admin@test.com");
        assertThat(tokenProvider.getRoleFromToken(token)).isEqualTo(RoleEnum.ADMIN);
    }
}