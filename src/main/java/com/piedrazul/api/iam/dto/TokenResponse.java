package com.piedrazul.api.iam.dto;

import com.piedrazul.api.iam.domain.RoleEnum;

/**
 * DTO de salida devuelto por AuthController tras un registro o login
 * exitoso. Contiene unicamente el token generado por TokenProvider
 */
public class TokenResponse {

    private String token;
    private RoleEnum role;

    public TokenResponse() {
    }

    public TokenResponse(String token, RoleEnum role) {
        this.token = token;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }
}
