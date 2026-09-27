package com.piedrazul.api.scheduling.domain;

import java.util.Objects;

/**
 * @file Doctor.java
 * @brief Entidad de dominio que representa a un médico o terapista.
 *
 * @details
 * En este primer corte no existe CRUD de médicos: las instancias se cargan
 * desde {@code infrastructure}. La identidad de la entidad está definida por
 * su {@code id}.
 */
public class Doctor {

    private String id;
    private String nombre;
    private boolean activo;

    /**
     * @brief Crea un doctor con sus datos básicos.
     *
     * @param id     Identificador único del doctor.
     * @param nombre Nombre del doctor.
     * @param activo Indica si el doctor está activo.
     */
    public Doctor(String id, String nombre, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.activo = activo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Doctor))
            return false;
        Doctor doctor = (Doctor) o;
        return Objects.equals(id, doctor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}