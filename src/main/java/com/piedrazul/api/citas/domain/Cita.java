package com.piedrazul.api.citas.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.piedrazul.api.citas.exception.CancelacionNoPermitidaException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private String pacienteId;

    @Column(name = "medico_id", nullable = false)
    private String medicoId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    @Column(name = "motivo_cancelacion", length = 500)
    private String motivoCancelacion;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    public Cita() {
    }

    public Cita(String pacienteId, String medicoId, LocalDate fecha, LocalTime hora, EstadoCita estado) {
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
    }

    /** Días mínimos de anticipación exigidos para cancelar una cita. */
    public static final int DIAS_MINIMOS_CANCELACION = 2;

    /**
     * Cancela la cita si cumple las reglas de negocio (RF6):
     * la justificación es obligatoria, la cita no debe estar ya cancelada ni completada
     * y debe faltar al menos {@value #DIAS_MINIMOS_CANCELACION} días para su inicio.
     */
    public void cancelar(String motivo, LocalDateTime ahora) {
        if (motivo == null || motivo.isBlank()) {
            throw new CancelacionNoPermitidaException("La justificación de la cancelación es obligatoria");
        }
        if (estado == EstadoCita.CANCELADA) {
            throw new CancelacionNoPermitidaException("La cita ya se encuentra cancelada");
        }
        if (estado == EstadoCita.COMPLETADA) {
            throw new CancelacionNoPermitidaException("No se puede cancelar una cita ya completada");
        }
        LocalDateTime inicio = LocalDateTime.of(fecha, hora);
        if (inicio.isBefore(ahora.plusDays(DIAS_MINIMOS_CANCELACION))) {
            throw new CancelacionNoPermitidaException(
                    "Solo se puede cancelar una cita con al menos " + DIAS_MINIMOS_CANCELACION + " días de anticipación");
        }
        this.estado = EstadoCita.CANCELADA;
        this.motivoCancelacion = motivo.trim();
        this.fechaCancelacion = ahora;
    }

    // Getters y Setters (sin cambios)
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

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }
}
