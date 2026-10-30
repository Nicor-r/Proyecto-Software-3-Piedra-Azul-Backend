package com.piedrazul.api.citas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.piedrazul.api.citas.domain.EstadoCita;

/**
 * Una fila de la tabla del historial de un paciente (HU-5, escenario 1):
 * fecha, hora, medico/terapista y estado.
 */
public record CitaHistorialDTO(
        LocalDate fecha,
        LocalTime hora,
        String medicoId,
        String medicoNombre,
        EstadoCita estado) {
}