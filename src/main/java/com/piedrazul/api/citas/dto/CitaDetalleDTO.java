package com.piedrazul.api.citas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaDetalleDTO(
    LocalDate fecha,
    LocalTime hora,
    String pacienteId,
    String pacienteIdentificacion,
    String pacienteNombre,
    String medicoId,
    String medicoNombre,
    String estado
) {}