package com.piedrazul.api.citas.dto;

/**
 * Una sugerencia del autocompletado de cedula (HU-5.1): solo cedula y nombre completo.
 */
public record PacienteSugerenciaDTO(String cedula, String nombreCompleto) {
}