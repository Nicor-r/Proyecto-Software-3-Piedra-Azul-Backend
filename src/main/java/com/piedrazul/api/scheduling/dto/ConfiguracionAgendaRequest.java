package com.piedrazul.api.scheduling.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;


/**
 * DTO de entrada para configurar la agenda de un medico/terapista
 * (AdminController/ConfiguracionAgendaController -> configurarAgenda()).
 *
 */
public class ConfiguracionAgendaRequest {
    @NotBlank
    private String doctorId;

    private Integer ventanaSemanas;

    private Set<DayOfWeek> diasAtencion;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    private Integer intervaloMinutos;

    public ConfiguracionAgendaRequest() {
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public Integer getVentanaSemanas() {
        return ventanaSemanas;
    }

    public void setVentanaSemanas(Integer ventanaSemanas) {
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

    public Integer getIntervaloMinutos() {
        return intervaloMinutos;
    }

    public void setIntervaloMinutos(Integer intervaloMinutos) {
        this.intervaloMinutos = intervaloMinutos;
    }
}
