package com.piedrazul.api.citas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReagendarCitaRequest {

    @NotNull(message = "El citaId es obligatorio")
    private Long citaId;

    @NotNull(message = "La nueva fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser pasada")
    private LocalDate nuevaFecha; // Formato: "YYYY-MM-DD"

    @NotNull(message = "La nueva hora es obligatoria")
    private LocalTime nuevaHora;  // Formato: "HH:mm"

    public ReagendarCitaRequest() {
    }

    public ReagendarCitaRequest(Long citaId, LocalDate nuevaFecha, LocalTime nuevaHora) {
        this.citaId = citaId;
        this.nuevaFecha = nuevaFecha;
        this.nuevaHora = nuevaHora;
    }

    public Long getCitaId() {
        return citaId;
    }

    public void setCitaId(Long citaId) {
        this.citaId = citaId;
    }

    public LocalDate getNuevaFecha() {
        return nuevaFecha;
    }

    public void setNuevaFecha(LocalDate nuevaFecha) {
        this.nuevaFecha = nuevaFecha;
    }

    public LocalTime getNuevaHora() {
        return nuevaHora;
    }

    public void setNuevaHora(LocalTime nuevaHora) {
        this.nuevaHora = nuevaHora;
    }
}
