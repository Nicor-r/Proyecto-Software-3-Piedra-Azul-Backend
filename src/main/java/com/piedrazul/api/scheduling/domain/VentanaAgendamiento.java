package com.piedrazul.api.scheduling.domain;

import java.util.Objects;

import com.piedrazul.api.scheduling.exception.SchedulingException;

/**
 * Criterios de aceptacion
 * - semanas > 0 -> configuracion exitosa
 * - semanas <= 0 -> "La ventana de agendamiento debe ser un numero entero
 * mayor a 0"
 */
public final class VentanaAgendamiento {

    private final int semanas;

    public VentanaAgendamiento(int semanas) {
        if (semanas <= 0) {
            throw new SchedulingException(
                    "La ventana de agendamiento debe ser un numero entero mayor a 0");
        }
        this.semanas = semanas;
    }

    public int getSemanas() {
        return semanas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof VentanaAgendamiento))
            return false;
        VentanaAgendamiento that = (VentanaAgendamiento) o;
        return semanas == that.semanas;
    }

    @Override
    public int hashCode() {
        return Objects.hash(semanas);
    }
}