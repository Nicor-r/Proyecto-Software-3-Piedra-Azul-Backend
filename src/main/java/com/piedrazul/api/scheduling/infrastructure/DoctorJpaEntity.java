package com.piedrazul.api.scheduling.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @file DoctorJpaEntity.java
 * @brief Entidad JPA que mapea la tabla {@code doctores}.
 */
@Entity
@Table(name = "doctores")
public class DoctorJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    // Id del User (modulo iam) con el que este medico inicia sesion. Opcional.
    @Column(name = "user_id", length = 36)
    private String userId;

    public DoctorJpaEntity() {
    }

    public DoctorJpaEntity(String id, String nombre, boolean activo) {
        this(id, nombre, activo, null);
    }

    public DoctorJpaEntity(String id, String nombre, boolean activo, String userId) {
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
}