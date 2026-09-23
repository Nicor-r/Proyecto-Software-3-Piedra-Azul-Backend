package com.piedrazul.api.scheduling.domain;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import com.piedrazul.api.scheduling.exception.SchedulingException;

/**
 * Value Object para HU Dias de la semana en que un medico/terapista atiende
 * un dia seleccionado -> Ok
 * ningun dia seleccionado -> Seleccionar almenos un dia
 */
public  final class  DiasAtencion {

    private final Set<DayOfWeek> dias;

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