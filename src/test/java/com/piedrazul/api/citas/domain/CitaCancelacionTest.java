package com.piedrazul.api.citas.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.piedrazul.api.citas.exception.CancelacionNoPermitidaException;

class CitaCancelacionTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 20, 10, 0);

    private Cita citaEn(LocalDateTime inicio, EstadoCita estado) {
        return new Cita("pac-1", "doc-1", inicio.toLocalDate(), inicio.toLocalTime(), estado);
    }

    @Test
    void conMasDeDosDiasDeAnticipacion_seCancela() {
        Cita cita = citaEn(AHORA.plusDays(5), EstadoCita.CONFIRMADA);

        cita.cancelar("Viaje de trabajo", AHORA);

        assertThat(cita.getEstado()).isEqualTo(EstadoCita.CANCELADA);
        assertThat(cita.getMotivoCancelacion()).isEqualTo("Viaje de trabajo");
        assertThat(cita.getFechaCancelacion()).isEqualTo(AHORA);
    }

    @Test
    void elMotivoSeGuardaSinEspaciosSobrantes() {
        Cita cita = citaEn(AHORA.plusDays(5), EstadoCita.CONFIRMADA);

        cita.cancelar("  Me mudé de ciudad  ", AHORA);

        assertThat(cita.getMotivoCancelacion()).isEqualTo("Me mudé de ciudad");
    }

    @Test
    void exactamenteDosDiasDeAnticipacion_esElLimiteYSePermite() {
        Cita cita = citaEn(AHORA.plusDays(2), EstadoCita.PENDIENTE);

        cita.cancelar("Cambio de planes", AHORA);

        assertThat(cita.getEstado()).isEqualTo(EstadoCita.CANCELADA);
    }

    @Test
    void conMenosDeDosDiasDeAnticipacion_seRechaza() {
        Cita cita = citaEn(AHORA.plusDays(2).minusMinutes(1), EstadoCita.CONFIRMADA);

        assertThatThrownBy(() -> cita.cancelar("Imprevisto", AHORA))
                .isInstanceOf(CancelacionNoPermitidaException.class)
                .hasMessageContaining("2 días");
        assertThat(cita.getMotivoCancelacion()).isNull();
        assertThat(cita.getFechaCancelacion()).isNull();
        assertThat(cita.getEstado()).isEqualTo(EstadoCita.CONFIRMADA);
    }

    @Test
    void citaEnElPasado_seRechaza() {
        Cita cita = citaEn(AHORA.minusDays(1), EstadoCita.CONFIRMADA);

        assertThatThrownBy(() -> cita.cancelar("Imprevisto", AHORA))
                .isInstanceOf(CancelacionNoPermitidaException.class);
    }

    @Test
    void sinJustificacion_seRechaza() {
        Cita cita = citaEn(AHORA.plusDays(5), EstadoCita.CONFIRMADA);

        assertThatThrownBy(() -> cita.cancelar(null, AHORA))
                .isInstanceOf(CancelacionNoPermitidaException.class)
                .hasMessageContaining("justificación");
        assertThatThrownBy(() -> cita.cancelar("   ", AHORA))
                .isInstanceOf(CancelacionNoPermitidaException.class);
        assertThat(cita.getEstado()).isEqualTo(EstadoCita.CONFIRMADA);
    }

    @Test
    void citaYaCancelada_seRechaza() {
        Cita cita = citaEn(AHORA.plusDays(5), EstadoCita.CANCELADA);

        assertThatThrownBy(() -> cita.cancelar("Otra vez", AHORA))
                .isInstanceOf(CancelacionNoPermitidaException.class)
                .hasMessageContaining("ya se encuentra cancelada");
    }

    @Test
    void citaCompletada_seRechaza() {
        Cita cita = citaEn(AHORA.plusDays(5), EstadoCita.COMPLETADA);

        assertThatThrownBy(() -> cita.cancelar("Error", AHORA))
                .isInstanceOf(CancelacionNoPermitidaException.class)
                .hasMessageContaining("completada");
    }
}
