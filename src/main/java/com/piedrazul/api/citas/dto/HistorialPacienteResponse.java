package com.piedrazul.api.citas.dto;

import java.util.List;

/**
 * Respuesta del historial de citas de un paciente (HU-5):
 * nombre del paciente, total de citas y las citas de la mas reciente a la mas antigua.
 */
public record HistorialPacienteResponse(
        String cedula,
        String nombrePaciente,
        int total,
        List<CitaHistorialDTO> citas) {
}