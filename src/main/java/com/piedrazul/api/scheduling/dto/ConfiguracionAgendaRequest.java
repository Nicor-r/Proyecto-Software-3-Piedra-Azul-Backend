package com.piedrazul.api.scheduling.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;

/**
 * @file ConfiguracionAgendaRequest.java
 * @brief DTO de entrada para configurar la agenda de un médico o terapista.
 *
 * @details
 *          Es el cuerpo esperado por
 *          {@code ConfiguracionAgendaController#configurar}.
 *          Solo {@code doctorId} se valida con Bean Validation en esta capa; el
 *          resto
 *          de los campos se validan en el dominio al construir los Value
 *          Objects
 *          correspondientes ({@code VentanaAgendamiento}, {@code DiasAtencion},
 *          {@code FranjaHoraria}, {@code IntervaloCitas}).
 */
public class ConfiguracionAgendaRequest {

    /**
     * @brief Identificador del doctor. Obligatorio.
     */
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