package com.piedrazul.api.citas.dto;

import java.time.LocalTime;

import com.piedrazul.api.citas.domain.EstadoCita;

/**
 * Un renglon del CSV de exportacion del medico (HU-4, escenario 1):
 * Hora, Paciente, Cedula, Telefono y Estado.
 */
public record FilaExportacionCita(
        LocalTime hora,
        String paciente,
        String cedula,
        String telefono,
        EstadoCita estado) {
}