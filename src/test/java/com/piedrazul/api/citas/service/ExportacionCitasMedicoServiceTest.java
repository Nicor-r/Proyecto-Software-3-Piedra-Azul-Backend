package com.piedrazul.api.citas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.piedrazul.api.citas.domain.Cita;
import com.piedrazul.api.citas.domain.EstadoCita;
import com.piedrazul.api.citas.dto.ArchivoCsv;
import com.piedrazul.api.citas.infrastructure.repository.CitaRepository;
import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.repository.UserRepository;
import com.piedrazul.api.scheduling.domain.Doctor;

@ExtendWith(MockitoExtension.class)
class ExportacionCitasMedicoServiceTest {

    private static final String EMAIL_MEDICO = "medico@piedrazul.com";
    private static final LocalDate FECHA = LocalDate.of(2026, 10, 30);
    private static final String ENCABEZADO = "Hora;Paciente;Cédula;Teléfono;Estado";

    @Mock
    private MedicoActualService medicoActualService;

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private UserRepository userRepository;

    private ExportacionCitasMedicoService service;

    private final Doctor doc4 = new Doctor("doc-4", "Dra. Ibis González - Cardiología", true, "user-123");
    private final User ana = new User("pac-1", "Ana Ruiz", "ana.ruiz@gmail.com", "1061000001",
            "3001112233", "hash", RoleEnum.PATIENT);
    private final User carlos = new User("pac-3", "Carlos Medina", "carlos.medina@gmail.com", "1061000003",
            "3003334455", "hash", RoleEnum.PATIENT);
    private final Cita citaAna = new Cita("ana.ruiz@gmail.com", "doc-4", FECHA, LocalTime.of(8, 0), EstadoCita.CONFIRMADA);
    private final Cita citaCarlos = new Cita("carlos.medina@gmail.com", "doc-4", FECHA, LocalTime.of(9, 0), EstadoCita.PENDIENTE);

    @BeforeEach
    void setUp() {
        // La "imprenta" es real (logica pura); los repositorios y el medico actual son mocks.
        service = new ExportacionCitasMedicoService(medicoActualService, citaRepository, userRepository,
                new ExportadorCsvCitas());
    }

    private String[] lineasDelCsv(ArchivoCsv archivo) {
        byte[] bytes = archivo.contenido();
        return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8).split("\r\n");
    }

    @Test
    void exportar_conCitas_generaArchivoConNombreDeHU4YUnaFilaPorCita() {
        when(medicoActualService.obtenerMedicoActual(EMAIL_MEDICO)).thenReturn(doc4);
        when(citaRepository.findByMedicoIdAndFechaOrderByHoraAsc("doc-4", FECHA)).thenReturn(List.of(citaAna, citaCarlos));
        when(userRepository.findByEmail("ana.ruiz@gmail.com")).thenReturn(Optional.of(ana));
        when(userRepository.findByEmail("carlos.medina@gmail.com")).thenReturn(Optional.of(carlos));

        ArchivoCsv archivo = service.exportarCitasDelMedico(EMAIL_MEDICO, FECHA, null);

        assertThat(archivo.nombreArchivo()).isEqualTo("citas_Dra_Ibis_Gonzalez_Cardiologia_2026-10-30.csv");
        assertThat(lineasDelCsv(archivo)).containsExactly(
                ENCABEZADO,
                "08:00;Ana Ruiz;1061000001;3001112233;CONFIRMADA",
                "09:00;Carlos Medina;1061000003;3003334455;PENDIENTE");
    }

    @Test
    void exportar_conEstadoFiltrado_usaLaConsultaConEstadoYSoloIncluyeEsasCitas() {
        when(medicoActualService.obtenerMedicoActual(EMAIL_MEDICO)).thenReturn(doc4);
        when(citaRepository.findByMedicoIdAndFechaAndEstadoOrderByHoraAsc("doc-4", FECHA, EstadoCita.CONFIRMADA))
                .thenReturn(List.of(citaAna));
        when(userRepository.findByEmail("ana.ruiz@gmail.com")).thenReturn(Optional.of(ana));

        ArchivoCsv archivo = service.exportarCitasDelMedico(EMAIL_MEDICO, FECHA, "confirmada");

        assertThat(lineasDelCsv(archivo)).containsExactly(
                ENCABEZADO,
                "08:00;Ana Ruiz;1061000001;3001112233;CONFIRMADA");
        verify(citaRepository, never()).findByMedicoIdAndFechaOrderByHoraAsc(any(), any());
    }

    @Test
    void exportar_conEstadoTodos_usaLaConsultaSinFiltro() {
        when(medicoActualService.obtenerMedicoActual(EMAIL_MEDICO)).thenReturn(doc4);
        when(citaRepository.findByMedicoIdAndFechaOrderByHoraAsc("doc-4", FECHA)).thenReturn(List.of(citaAna));
        when(userRepository.findByEmail("ana.ruiz@gmail.com")).thenReturn(Optional.of(ana));

        service.exportarCitasDelMedico(EMAIL_MEDICO, FECHA, "TODOS");

        verify(citaRepository, never()).findByMedicoIdAndFechaAndEstadoOrderByHoraAsc(any(), any(), any());
    }

    @Test
    void exportar_sinCitasEnLaFecha_lanzaMensajeSinCitas() {
        when(medicoActualService.obtenerMedicoActual(EMAIL_MEDICO)).thenReturn(doc4);
        when(citaRepository.findByMedicoIdAndFechaOrderByHoraAsc("doc-4", FECHA)).thenReturn(List.of());

        assertThatThrownBy(() -> service.exportarCitasDelMedico(EMAIL_MEDICO, FECHA, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ExportacionCitasMedicoService.MENSAJE_SIN_CITAS);
    }

    @Test
    void exportar_estadoSinCitas_lanzaMensajeEstadoSinCitas() {
        when(medicoActualService.obtenerMedicoActual(EMAIL_MEDICO)).thenReturn(doc4);
        when(citaRepository.findByMedicoIdAndFechaAndEstadoOrderByHoraAsc("doc-4", FECHA, EstadoCita.CANCELADA))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.exportarCitasDelMedico(EMAIL_MEDICO, FECHA, "CANCELADA"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ExportacionCitasMedicoService.MENSAJE_ESTADO_SIN_CITAS);
    }

    @Test
    void exportar_sinFecha_lanzaMensajeFiltroIncompletoSinConsultar() {
        assertThatThrownBy(() -> service.exportarCitasDelMedico(EMAIL_MEDICO, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ExportacionCitasMedicoService.MENSAJE_FILTRO_INCOMPLETO);

        verify(medicoActualService, never()).obtenerMedicoActual(any());
    }

    @Test
    void exportar_medicoNoDisponible_propagaElMensajeYNoBuscaCitas() {
        when(medicoActualService.obtenerMedicoActual(EMAIL_MEDICO))
                .thenThrow(new IllegalArgumentException(MedicoActualService.MENSAJE_MEDICO_NO_DISPONIBLE));

        assertThatThrownBy(() -> service.exportarCitasDelMedico(EMAIL_MEDICO, FECHA, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MedicoActualService.MENSAJE_MEDICO_NO_DISPONIBLE);

        verify(citaRepository, never()).findByMedicoIdAndFechaOrderByHoraAsc(any(), any());
    }

    @Test
    void exportar_estadoInvalido_lanzaMensajeSinConsultar() {
        assertThatThrownBy(() -> service.exportarCitasDelMedico(EMAIL_MEDICO, FECHA, "ATENDIDA"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Estado de cita inválido");

        verify(medicoActualService, never()).obtenerMedicoActual(any());
    }

    @Test
    void exportar_pacienteNoEncontrado_usaElIdentificadorDeLaCitaYDejaVaciosSusDatos() {
        Cita citaHuerfana = new Cita("desconocido@gmail.com", "doc-4", FECHA, LocalTime.of(10, 0), EstadoCita.CONFIRMADA);
        when(medicoActualService.obtenerMedicoActual(EMAIL_MEDICO)).thenReturn(doc4);
        when(citaRepository.findByMedicoIdAndFechaOrderByHoraAsc("doc-4", FECHA)).thenReturn(List.of(citaHuerfana));
        when(userRepository.findByEmail("desconocido@gmail.com")).thenReturn(Optional.empty());

        ArchivoCsv archivo = service.exportarCitasDelMedico(EMAIL_MEDICO, FECHA, null);

        assertThat(lineasDelCsv(archivo)[1]).isEqualTo("10:00;desconocido@gmail.com;;;CONFIRMADA");
    }
}