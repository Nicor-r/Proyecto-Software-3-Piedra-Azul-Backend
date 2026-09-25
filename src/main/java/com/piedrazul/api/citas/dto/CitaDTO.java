package com.piedrazul.api.citas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.piedrazul.api.citas.domain.EstadoCita;

public class CitaDTO {
    private Long id;
    private String pacienteId;
    private String medicoId;
    private LocalDate fecha;
    private LocalTime hora;
    private EstadoCita estado;

    public CitaDTO() {}

    public CitaDTO(Long id, String pacienteId, String medicoId,
                   LocalDate fecha, LocalTime hora, EstadoCita estado) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(String pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getMedicoId() {
        return medicoId;
    }

    public void setMedicoId(String medicoId) {
        this.medicoId = medicoId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }
}