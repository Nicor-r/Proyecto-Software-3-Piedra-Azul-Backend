package com.piedrazul.api.scheduling.domain;

import java.util.Objects;

import com.piedrazul.api.scheduling.exception.SchedulingException;

/**
 * IntervaloCitas
 * - valor dentro del rango -> configuracion exitosa
 * - valor negativo, cero, no numerico o fuera de rango -> El intervalo
 * debe ser un valor numerico entre MINIMO y MAXIMO minutos
 */
public final class IntervaloCitas {

    private static final int MINIMO_MINUTOS = 10;
    private static final int MAXIMO_MINUTOS = 120;

    private final int minutos;

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