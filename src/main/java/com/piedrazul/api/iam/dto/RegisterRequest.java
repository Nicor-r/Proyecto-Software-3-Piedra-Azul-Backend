package com.piedrazul.api.iam.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para el registro de pacientes (AuthController -> registerPatient()).
 *
 * No incluye "role": el registro publico corresponde unicamente a pacientes
 * (ver seccion 8, AuthService.registerPatient()). El rol PATIENT se asigna
 * internamente en la capa service, no lo decide el cliente de la API.
 *
 * Solo contiene validaciones sintacticas basicas (@NotBlank, @Email),
 * segun la responsabilidad del controller definida en la seccion 3.
 */
public class RegisterRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    public RegisterRequest() {
    }

    public RegisterRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
