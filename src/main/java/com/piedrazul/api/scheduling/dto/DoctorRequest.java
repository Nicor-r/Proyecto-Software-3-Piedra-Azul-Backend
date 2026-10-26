package com.piedrazul.api.scheduling.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @file DoctorRequest.java
 * @brief DTO de entrada para crear o actualizar un doctor.
 */
public class DoctorRequest {

    @NotBlank(message = "El nombre del medico/terapista es obligatorio")
    private String nombre;

    private boolean activo = true;

    public DoctorRequest() {
    }

    public DoctorRequest(String nombre, boolean activo) {
        this.nombre = nombre;
        this.activo = activo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
