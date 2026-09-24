package com.piedrazul.api.scheduling.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public class ConfiguracionAgendaResponse {
    private String id;
    private String doctorId;
    private int ventanaSemanas;
    private Set<DayOfWeek> diasAtencion;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int intervaloMinutos;

    public ConfiguracionAgendaResponse() {
    }

    public ConfiguracionAgendaResponse(String id, String doctorId, int ventanaSemanas,
            Set<DayOfWeek> diasAtencion, LocalTime horaInicio,
            LocalTime horaFin, int intervaloMinutos) {
        this.id = id;
        this.doctorId = doctorId;
        this.ventanaSemanas = ventanaSemanas;
        this.diasAtencion = diasAtencion;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.intervaloMinutos = intervaloMinutos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public int getVentanaSemanas() {
        return ventanaSemanas;
    }

    public void setVentanaSemanas(int ventanaSemanas) {
        this.ventanaSemanas = ventanaSemanas;
    }

    public Set<DayOfWeek> getDiasAtencion() {
        return diasAtencion;
    }

    public void setDiasAtencion(Set<DayOfWeek> diasAtencion) {
        this.diasAtencion = diasAtencion;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public int getIntervaloMinutos() {
        return intervaloMinutos;
    }

    public void setIntervaloMinutos(int intervaloMinutos) {
        this.intervaloMinutos = intervaloMinutos;
    }
}
