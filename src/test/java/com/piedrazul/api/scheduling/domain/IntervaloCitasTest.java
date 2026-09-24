package com.piedrazul.api.scheduling.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.piedrazul.api.scheduling.exception.SchedulingException;

class IntervaloCitasTest {

    @Test
    void valorDentroDelRango_seCreaCorrectamente() {
        IntervaloCitas intervalo = new IntervaloCitas(30);

        assertThat(intervalo.getMinutos()).isEqualTo(30);
    }

    @Test
    void valorCero_lanzaSchedulingException() {
        assertThrows(SchedulingException.class, () -> new IntervaloCitas(0));
    }

    @Test
    void valorNegativo_lanzaSchedulingException() {
        assertThrows(SchedulingException.class, () -> new IntervaloCitas(-5));
    }

    @Test
    void valorFueraDeRangoSuperior_lanzaSchedulingException() {
        SchedulingException exception = assertThrows(SchedulingException.class,
                () -> new IntervaloCitas(150));

        assertThat(exception.getMessage())
                .isEqualTo("El intervalo debe ser un valor numerico entre 10 y 120 minutos");
    }
}
