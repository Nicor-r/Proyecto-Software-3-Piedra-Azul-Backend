package com.piedrazul.api.scheduling.domain;

import java.util.Objects;

/**
 * @file Doctor.java
 * @brief Entidad de dominio que representa a un médico o terapista.
 *
 * @details
 *          La identidad de la entidad está definida por su {@code id} (ej.
 *          "doc-4").
 *          El campo {@code userId} es OPCIONAL: vincula esta ficha con la
 *          cuenta de
 *          usuario (modulo iam) con la que el medico inicia sesion. Si es null,
 *          el
 *          doctor existe en la agenda pero no tiene cuenta para ingresar al
 *          sistema.
 */
public class Doctor {

    private String id;
    private String nombre;
    private boolean activo;
    private String userId;

    /**
     * @brief Crea un doctor SIN cuenta de usuario vinculada (comportamiento
     *        original).
     */
    public Doctor(String id, String nombre, boolean activo) {
        this(id, nombre, activo, null);
    }

    /**
     * @brief Crea un doctor vinculado a una cuenta de usuario.
     *
     * @param userId Id del User (modulo iam) del medico; puede ser null.
     */
    public Doctor(String id, String nombre, boolean activo, String userId) {
        this.id = id;
        this.nombre = nombre;
        this.activo = activo;
        this.userId = userId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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