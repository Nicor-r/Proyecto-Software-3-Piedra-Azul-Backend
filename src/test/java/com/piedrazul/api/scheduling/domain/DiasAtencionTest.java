package com.piedrazul.api.scheduling.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.piedrazul.api.scheduling.exception.SchedulingException;

class DiasAtencionTest {

    @Test
    void alMenosUnDiaSeleccionado_seCreaCorrectamente() {
        Set<DayOfWeek> seleccion = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);

        DiasAtencion dias = new DiasAtencion(seleccion);

        assertThat(dias.getDias()).containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
    }

    @Test
    void conjuntoVacio_lanzaSchedulingException() {
        SchedulingException exception = assertThrows(SchedulingException.class,
                () -> new DiasAtencion(Collections.emptySet()));

        assertThat(exception.getMessage()).isEqualTo("Debe seleccionar al menos un dia de atencion");
    }

    @Test
    void conjuntoNulo_lanzaSchedulingException() {
        assertThrows(SchedulingException.class, () -> new DiasAtencion(null));
    }
}
