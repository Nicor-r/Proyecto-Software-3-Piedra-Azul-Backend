package com.piedrazul.api.citas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.piedrazul.api.citas.domain.Cita;
import com.piedrazul.api.citas.domain.EstadoCita;
import com.piedrazul.api.citas.dto.CitaDTO;
import com.piedrazul.api.citas.exception.CancelacionNoPermitidaException;
import com.piedrazul.api.citas.infrastructure.repository.CitaRepository;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.infrastructure.repository.SpringDataTerapistaRepository;
import com.piedrazul.api.iam.repository.UserRepository;
import com.piedrazul.api.scheduling.repository.ConfiguracionAgendaRepository;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

@ExtendWith(MockitoExtension.class)
class CitaServiceCancelarTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 20, 10, 0);
    private static final String PACIENTE = "paciente@piedrazul.com";
    private static final long CITA_ID = 7L;

    @Mock
    private CitaRepository citaRepository;
    @Mock
    private ConfiguracionAgendaRepository configuracionAgendaRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private SpringDataTerapistaRepository terapistaRepository;
    @Mock
    private UserRepository userRepository;

    private CitaService service;

    @BeforeEach
    void setUp() {
        service = new CitaService(citaRepository, configuracionAgendaRepository, doctorRepository,
                terapistaRepository, userRepository);
    }

    private Cita cita(String pacienteId, LocalDateTime inicio, EstadoCita estado) {
        Cita c = new Cita(pacienteId, "doc-1", inicio.toLocalDate(), inicio.toLocalTime(), estado);
        c.setId(CITA_ID);
        return c;
    }

    @Test
    void cancelarCita_propia_conAnticipacion_cancelaYGuarda() {
        Cita cita = cita(PACIENTE, AHORA.plusDays(5), EstadoCita.CONFIRMADA);
        when(citaRepository.findById(CITA_ID)).thenReturn(Optional.of(cita));
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        CitaDTO dto = service.cancelarCita(CITA_ID, PACIENTE, "Viaje", AHORA);

        assertThat(dto.getId()).isEqualTo(CITA_ID);
        assertThat(dto.getEstado()).isEqualTo(EstadoCita.CANCELADA);
        assertThat(cita.getMotivoCancelacion()).isEqualTo("Viaje");
        assertThat(cita.getFechaCancelacion()).isEqualTo(AHORA);
        verify(citaRepository).save(cita);
    }

    @Test
    void cancelarCita_guardadaConIdDelPaciente_tambienEsDelPaciente() {
        Cita cita = cita("uuid-paciente-1", AHORA.plusDays(5), EstadoCita.PENDIENTE);
        User paciente = org.mockito.Mockito.mock(User.class);
        when(paciente.getId()).thenReturn("uuid-paciente-1");
        when(userRepository.findByEmail(PACIENTE)).thenReturn(Optional.of(paciente));
        when(citaRepository.findById(CITA_ID)).thenReturn(Optional.of(cita));
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        CitaDTO dto = service.cancelarCita(CITA_ID, PACIENTE, "Cambio de planes", AHORA);

        assertThat(dto.getEstado()).isEqualTo(EstadoCita.CANCELADA);
    }

    @Test
    void cancelarCita_inexistente_lanza404() {
        when(citaRepository.findById(CITA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelarCita(CITA_ID, PACIENTE, "Motivo", AHORA))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void cancelarCita_deOtroPaciente_lanza403SinModificarla() {
        Cita ajena = cita("otro@piedrazul.com", AHORA.plusDays(5), EstadoCita.CONFIRMADA);
        when(citaRepository.findById(CITA_ID)).thenReturn(Optional.of(ajena));
        when(userRepository.findByEmail(PACIENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelarCita(CITA_ID, PACIENTE, "Motivo", AHORA))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
        assertThat(ajena.getEstado()).isEqualTo(EstadoCita.CONFIRMADA);
        verify(citaRepository, never()).save(any());
    }

    @Test
    void cancelarCita_conMenosDeDosDias_propagaLaRegla_yNoGuarda() {
        Cita cita = cita(PACIENTE, AHORA.plusDays(1), EstadoCita.CONFIRMADA);
        when(citaRepository.findById(CITA_ID)).thenReturn(Optional.of(cita));

        assertThatThrownBy(() -> service.cancelarCita(CITA_ID, PACIENTE, "Imprevisto", AHORA))
                .isInstanceOf(CancelacionNoPermitidaException.class);
        assertThat(cita.getEstado()).isEqualTo(EstadoCita.CONFIRMADA);
        verify(citaRepository, never()).save(any());
    }

    @Test
    void cancelarCita_sinMotivo_propagaLaRegla_yNoGuarda() {
        Cita cita = cita(PACIENTE, AHORA.plusDays(5), EstadoCita.CONFIRMADA);
        when(citaRepository.findById(CITA_ID)).thenReturn(Optional.of(cita));

        assertThatThrownBy(() -> service.cancelarCita(CITA_ID, PACIENTE, "  ", AHORA))
                .isInstanceOf(CancelacionNoPermitidaException.class);
        verify(citaRepository, never()).save(any());
    }

    @Test
    void cancelarCita_pacienteNoAutenticado_lanzaIllegalArgument() {
        assertThatThrownBy(() -> service.cancelarCita(CITA_ID, " ", "Motivo", AHORA))
                .isInstanceOf(IllegalArgumentException.class);
        verify(citaRepository, never()).findById(any());
    }
}
