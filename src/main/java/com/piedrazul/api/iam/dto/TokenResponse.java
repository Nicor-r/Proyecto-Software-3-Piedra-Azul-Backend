package com.piedrazul.api.iam.dto;

/**
 * DTO de salida devuelto por AuthController tras un registro o login
 * exitoso. Contiene unicamente el token generado por TokenProvider
 * (ver seccion 11 del documento arquitectonico).
 */
public class TokenResponse {

    private String token;

    public TokenResponse() {
    }

    public TokenResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
