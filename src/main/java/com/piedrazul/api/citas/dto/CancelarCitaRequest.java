package com.piedrazul.api.citas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CancelarCitaRequest {

    @NotBlank(message = "La justificación de la cancelación es obligatoria")
    @Size(max = 500, message = "La justificación no puede superar los 500 caracteres")
    private String motivo;

    public CancelarCitaRequest() {
    }

    public CancelarCitaRequest(String motivo) {
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
