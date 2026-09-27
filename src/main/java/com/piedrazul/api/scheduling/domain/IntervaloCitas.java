package com.piedrazul.api.scheduling.domain;

import java.util.Objects;

import com.piedrazul.api.scheduling.exception.SchedulingException;

/**
 * @file IntervaloCitas.java
 * @brief Value Object que representa el intervalo, en minutos, entre citas.
 *
 * @details
 * Invariante: el valor debe estar dentro del rango cerrado
 * [{@value #MINIMO_MINUTOS}, {@value #MAXIMO_MINUTOS}] minutos.
 *
 * Al ser inmutable, la instancia es segura de compartir entre hilos.
 */
public final class IntervaloCitas {

    private static final int MINIMO_MINUTOS = 10;
    private static final int MAXIMO_MINUTOS = 120;

    private final int minutos;

    /**
     * @brief Crea el intervalo a partir del número de minutos.
     *
     * @param minutos Duración del intervalo en minutos. Debe estar entre
     *                {@value #MINIMO_MINUTOS} y {@value #MAXIMO_MINUTOS}.
     * @throws SchedulingException Si {@code minutos} está fuera del rango permitido.
     */
    public IntervaloCitas(int minutos) {
        if (minutos < MINIMO_MINUTOS || minutos > MAXIMO_MINUTOS) {
            throw new SchedulingException(
                    "El intervalo debe ser un valor numerico entre " + MINIMO_MINUTOS
                            + " y " + MAXIMO_MINUTOS + " minutos");
        }
        this.minutos = minutos;
    }

    public int getMinutos() {
        return minutos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof IntervaloCitas))
            return false;
        IntervaloCitas that = (IntervaloCitas) o;
        return minutos == that.minutos;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minutos);
    }
}