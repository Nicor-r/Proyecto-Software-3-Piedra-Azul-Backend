package com.piedrazul.api.citas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CancelarCitaRequest {

    @NotNull(message = "El citaId es obligatorio")
    private Long citaId;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;
    
    public CancelarCitaRequest() {
    }

    public CancelarCitaRequest(Long citaId, String motivo) {
        this.citaId = citaId;
        this.motivo = motivo;
    }

    // Getters y Setters
    public Long getCitaId() {
        return citaId;
    }

    public void setCitaId(Long citaId) {
        this.citaId = citaId;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}