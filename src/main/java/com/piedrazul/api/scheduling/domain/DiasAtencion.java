package com.piedrazul.api.scheduling.domain;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import com.piedrazul.api.scheduling.exception.SchedulingException;

/**
 * @file DiasAtencion.java
 * @brief Value Object que representa los días de la semana en que un médico
 *        o terapista atiende.
 *
 * @details
 * Invariante: debe contener al menos un día. La colección es inmutable una vez
 * construida, por lo que la instancia es segura de compartir entre hilos.
 */
public final class DiasAtencion {

    private final Set<DayOfWeek> dias;

    /**
     * @brief Crea el Value Object a partir del conjunto de días de atención.
     *
     * @param dias Días seleccionados. No puede ser {@code null} ni vacío.
     * @throws SchedulingException Si {@code dias} es {@code null} o está vacío.
     */
    public DiasAtencion(Set<DayOfWeek> dias) {
        if (dias == null || dias.isEmpty()) {
            throw new SchedulingException("Debe seleccionar al menos un dia de atencion");
        }
        this.dias = Collections.unmodifiableSet(EnumSet.copyOf(dias));
    }

    public Set<DayOfWeek> getDias() {
        return dias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DiasAtencion))
            return false;
        DiasAtencion that = (DiasAtencion) o;
        return Objects.equals(dias, that.dias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dias);
    }
}