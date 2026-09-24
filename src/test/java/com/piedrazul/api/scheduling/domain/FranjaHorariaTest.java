package com.piedrazul.api.scheduling.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.piedrazul.api.scheduling.exception.SchedulingException;

class FranjaHorariaTest {

    @Test
    void horaInicioAntesDeHoraFin_seCreaCorrectamente() {
        FranjaHoraria franja = new FranjaHoraria(LocalTime.of(8, 0), LocalTime.of(17, 0));

        assertThat(franja.getHoraInicio()).isEqualTo(LocalTime.of(8, 0));
        assertThat(franja.getHoraFin()).isEqualTo(LocalTime.of(17, 0));
    }

    @Test
    void horaInicioIgualAHoraFin_lanzaSchedulingException() {
        SchedulingException exception = assertThrows(SchedulingException.class,
                () -> new FranjaHoraria(LocalTime.of(9, 0), LocalTime.of(9, 0)));

        assertThat(exception.getMessage()).isEqualTo("La hora de inicio debe ser anterior a la hora de fin");
    }

    @Test
    void horaInicioDespuesDeHoraFin_lanzaSchedulingException() {
        assertThrows(SchedulingException.class,
                () -> new FranjaHoraria(LocalTime.of(18, 0), LocalTime.of(8, 0)));
    }

    @Test
    void horaInicioNula_lanzaSchedulingException() {
        assertThrows(SchedulingException.class,
                () -> new FranjaHoraria(null, LocalTime.of(9, 0)));
    }

    @Test
    void horaFinNula_lanzaSchedulingException() {
        assertThrows(SchedulingException.class,
                () -> new FranjaHoraria(LocalTime.of(9, 0), null));
    }
}
