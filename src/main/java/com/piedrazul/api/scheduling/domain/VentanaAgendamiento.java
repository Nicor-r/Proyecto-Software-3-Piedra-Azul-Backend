package com.piedrazul.api.scheduling.domain;

import java.util.Objects;

import com.piedrazul.api.scheduling.exception.SchedulingException;

/**
 * @file VentanaAgendamiento.java
 * @brief Value Object que representa la ventana de agendamiento, expresada en
 *        semanas.
 *
 * @details
 * Invariante: el número de semanas debe ser un entero estrictamente mayor que
 * cero.
 *
 * Al ser inmutable, la instancia es segura de compartir entre hilos.
 */
public final class VentanaAgendamiento {

    private final int semanas;

    /**
     * @brief Crea la ventana de agendamiento a partir del número de semanas.
     *
     * @param semanas Número de semanas de la ventana. Debe ser mayor que cero.
     * @throws SchedulingException Si {@code semanas} es menor o igual a cero.
     */
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