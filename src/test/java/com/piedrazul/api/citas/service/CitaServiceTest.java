package com.piedrazul.api.citas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.piedrazul.api.citas.domain.Cita;
import com.piedrazul.api.citas.domain.EstadoCita;
import com.piedrazul.api.citas.dto.AgendarCitaRequest;
import com.piedrazul.api.citas.dto.CitaDTO;
import com.piedrazul.api.citas.dto.FranjaDTO;
import com.piedrazul.api.citas.exception.FranjaNoDisponibleException;
import com.piedrazul.api.citas.infrastructure.repository.CitaRepository;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    private CitaService service;

    private final String MEDICO_ID = "doc-1";
    private final LocalDate FECHA_FUTURA = LocalDate.now().plusDays(3);
    private final String PACIENTE_ID = "paciente@piedrazul.com";

    @BeforeEach
    void setUp() {
        service = new CitaService(citaRepository);
    }

    @Test
    void obtenerFranjasDisponibles_retornaFranjasConDisponibilidadCorrecta() {
        Cita citaOcupada = new Cita(PACIENTE_ID, MEDICO_ID, FECHA_FUTURA, LocalTime.of(8, 0), EstadoCita.CONFIRMADA);
        when(citaRepository.findByMedicoIdAndFecha(MEDICO_ID, FECHA_FUTURA))
                .thenReturn(List.of(citaOcupada));

        List<FranjaDTO> franjas = service.obtenerFranjasDisponibles(MEDICO_ID, FECHA_FUTURA);

        assertThat(franjas).isNotEmpty();
        FranjaDTO franja8am = franjas.stream()
                .filter(f -> f.getHora().equals(LocalTime.of(8, 0)))
                .findFirst()
                .orElseThrow();
        assertThat(franja8am.isDisponible()).isFalse();

        FranjaDTO franja830am = franjas.stream()
                .filter(f -> f.getHora().equals(LocalTime.of(8, 30)))
                .findFirst()
                .orElseThrow();
        assertThat(franja830am.isDisponible()).isTrue();
    }

    @Test
    void obtenerFranjasDisponibles_fechaPasada_lanzaExcepcion() {
        LocalDate fechaPasada = LocalDate.now().minusDays(1);

        assertThatThrownBy(() -> service.obtenerFranjasDisponibles(MEDICO_ID, fechaPasada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se pueden consultar fechas pasadas");
    }

    @Test
    void agendarCita_franjaDisponible_guardaYRetornaDTO() {
        LocalTime hora = LocalTime.of(9, 0);
        AgendarCitaRequest request = new AgendarCitaRequest(MEDICO_ID, FECHA_FUTURA, hora);

        when(citaRepository.existsByMedicoIdAndFechaAndHora(MEDICO_ID, FECHA_FUTURA, hora))
                .thenReturn(false);

        Cita citaGuardada = new Cita(PACIENTE_ID, MEDICO_ID, FECHA_FUTURA, hora, EstadoCita.PENDIENTE);
        citaGuardada.setId(10L);
        when(citaRepository.save(any(Cita.class))).thenReturn(citaGuardada);

        CitaDTO dto = service.agendarCita(request, PACIENTE_ID);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getPacienteId()).isEqualTo(PACIENTE_ID);
        assertThat(dto.getEstado()).isEqualTo(EstadoCita.PENDIENTE);
    }

    @Test
    void agendarCita_franjaOcupada_lanzaFranjaNoDisponibleException() {
        LocalTime hora = LocalTime.of(9, 0);
        AgendarCitaRequest request = new AgendarCitaRequest(MEDICO_ID, FECHA_FUTURA, hora);

        when(citaRepository.existsByMedicoIdAndFechaAndHora(MEDICO_ID, FECHA_FUTURA, hora))
                .thenReturn(true);

        assertThatThrownBy(() -> service.agendarCita(request, PACIENTE_ID))
                .isInstanceOf(FranjaNoDisponibleException.class)
                .hasMessageContaining("ya está ocupada");
    }

    @Test
    void obtenerMisCitas_retornaListaDeCitasDelPaciente() {
        Cita cita = new Cita(PACIENTE_ID, MEDICO_ID, FECHA_FUTURA, LocalTime.of(10, 0), EstadoCita.CONFIRMADA);
        cita.setId(1L);
        when(citaRepository.findByPacienteId(PACIENTE_ID)).thenReturn(List.of(cita));

        List<CitaDTO> misCitas = service.obtenerMisCitas(PACIENTE_ID);

        assertThat(misCitas).hasSize(1);
        assertThat(misCitas.get(0).getId()).isEqualTo(1L);
    }
}
