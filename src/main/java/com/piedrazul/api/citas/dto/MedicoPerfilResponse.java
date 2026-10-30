package com.piedrazul.api.citas.dto;

/**
 * Datos basicos del medico autenticado. El frontend los usa para mostrar
 * quien inicio sesion.
 */
public record MedicoPerfilResponse(String doctorId, String nombre) {
}