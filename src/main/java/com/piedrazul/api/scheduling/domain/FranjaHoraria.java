package com.piedrazul.api.scheduling.domain;

import java.time.LocalTime;
import java.util.Objects;

import com.piedrazul.api.scheduling.exception.SchedulingException;

/**
 * Value Object para HU franja horaria (hora de inicio y hora de fin)
 * criterios aceptacion
 * horainicio >= horafin -> La hora de inicio debe ser anterior a la hora de fin
 */
public final class FranjaHoraria {

    private final LocalTime horaInicio;
    private final LocalTime horaFin;

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