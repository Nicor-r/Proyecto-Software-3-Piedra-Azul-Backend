package com.piedrazul.api.citas.dto;

import java.time.LocalTime;

public class FranjaDTO {
    private LocalTime hora;
    private boolean disponible;

    public FranjaDTO(LocalTime hora, boolean disponible) {
        this.hora = hora;
        this.disponible = disponible;
    }

    // Getters y Setters

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}