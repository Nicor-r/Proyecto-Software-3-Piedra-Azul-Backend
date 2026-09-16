package com.piedrazul.api.iam.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para el registro de pacientes (AuthController -> registerPatient()).
 * No incluye "role": el registro publico corresponde unicamente a pacientes
 * internamente en la capa service, no lo decide el cliente de la API.
 */
public class RegisterRequest {

    @NotBlank
    private String nombreCompleto;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String numeroIdentificacion;

    @NotBlank
    private String numeroTelefonico;

    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;

    public RegisterRequest() {
    }

    public RegisterRequest(String nombreCompleto, String email, String numeroIdentificacion,
                           String numeroTelefonico, String password, String confirmPassword) {
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.numeroIdentificacion = numeroIdentificacion;
        this.numeroTelefonico = numeroTelefonico;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public String getNumeroTelefonico() {
        return numeroTelefonico;
    }

    public void setNumeroTelefonico(String numeroTelefonico) {
        this.numeroTelefonico = numeroTelefonico;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
