package com.piedrazul.api.iam.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "terapistas")
public class TerapistaJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "especialidad")
    private String especialidad;

    public TerapistaJpaEntity() {
    }

    public TerapistaJpaEntity(String id, String userId, String nombre, String especialidad) {
        this.id = id;
        this.userId = userId;
        this.nombre = nombre;
        this.especialidad = especialidad;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
}