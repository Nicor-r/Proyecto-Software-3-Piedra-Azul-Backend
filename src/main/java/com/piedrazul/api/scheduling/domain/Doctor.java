package com.piedrazul.api.scheduling.domain;

import java.util.Objects;

/**
 * Entidad de dominio Doctor (medico/terapista)
 * Para primer corte no hay CRUD de medicos, los vamos a "quemar" desde
 * infrastructure
 */
public class Doctor {

    private String id;
    private String nombre;
    private boolean activo;

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
