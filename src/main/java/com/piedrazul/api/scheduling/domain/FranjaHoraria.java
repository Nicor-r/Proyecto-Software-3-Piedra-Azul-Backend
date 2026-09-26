package com.piedrazul.api.scheduling.domain;

import java.time.LocalTime;
import java.util.Objects;

import com.piedrazul.api.scheduling.exception.SchedulingException;

/**
 * @file FranjaHoraria.java
 * @brief Value Object que representa la franja horaria de atención, definida
 *        por una hora de inicio y una hora de fin.
 *
 * @details
 * Invariantes:
 * - Ambas horas son obligatorias.
 * - La hora de inicio debe ser estrictamente anterior a la hora de fin.
 *
 * Al ser inmutable, la instancia es segura de compartir entre hilos.
 */
public final class FranjaHoraria {

    private final LocalTime horaInicio;
    private final LocalTime horaFin;

    /**
     * @brief Crea la franja horaria a partir de la hora de inicio y la hora de fin.
     *
     * @param horaInicio Hora de inicio. No puede ser {@code null}.
     * @param horaFin    Hora de fin. No puede ser {@code null} ni anterior o igual
     *                   a {@code horaInicio}.
     * @throws SchedulingException Si alguna hora es {@code null} o si
     *         {@code horaInicio} no es anterior a {@code horaFin}.
     */
    public FranjaHoraria(LocalTime horaInicio, LocalTime horaFin) {
        if (horaInicio == null || horaFin == null) {
            throw new SchedulingException(
                    "El campo es obligatorio: hora de inicio y hora de fin son requeridas");
        }
        if (!horaInicio.isBefore(horaFin)) {
            throw new SchedulingException("La hora de inicio debe ser anterior a la hora de fin");
        }
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FranjaHoraria))
            return false;
        FranjaHoraria that = (FranjaHoraria) o;
        return Objects.equals(horaInicio, that.horaInicio) && Objects.equals(horaFin, that.horaFin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(horaInicio, horaFin);
    }
}