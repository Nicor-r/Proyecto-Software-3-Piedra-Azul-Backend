package com.piedrazul.api.scheduling.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.piedrazul.api.scheduling.exception.SchedulingException;

class VentanaAgendamientoTest {

    @Test
    void semanasMayorACero_seCreaCorrectamente() {
        VentanaAgendamiento ventana = new VentanaAgendamiento(4);

        assertThat(ventana.getSemanas()).isEqualTo(4);
    }

    @Test
    void semanasEnCero_lanzaSchedulingException() {
        SchedulingException exception = assertThrows(SchedulingException.class,
                () -> new VentanaAgendamiento(0));

        assertThat(exception.getMessage())
                .isEqualTo("La ventana de agendamiento debe ser un numero entero mayor a 0");
    }

    @Test
    void semanasNegativas_lanzaSchedulingException() {
        assertThrows(SchedulingException.class, () -> new VentanaAgendamiento(-1));
    }
}
