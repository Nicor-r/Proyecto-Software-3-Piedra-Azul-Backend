package com.piedrazul.api.citas.dto;

import java.util.List;

/**
 * la tabla de citas encontradas junto con el
 * numero total, tal como pide el criterio de aceptacion "Listado exitoso".
 */
public class ListadoCitasResponse {
    private int total;
    private List<CitaDTO> citas;

    public ListadoCitasResponse() {
    }

    public ListadoCitasResponse(int total, List<CitaDTO> citas) {
        this.total = total;
        this.citas = citas;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<CitaDTO> getCitas() {
        return citas;
    }

    public void setCitas(List<CitaDTO> citas) {
        this.citas = citas;
    }
}
