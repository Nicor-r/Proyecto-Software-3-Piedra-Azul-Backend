package com.piedrazul.api.citas.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.piedrazul.api.citas.domain.Cita;
import com.piedrazul.api.citas.domain.EstadoCita;
import com.piedrazul.api.citas.dto.AgendarCitaRequest;
import com.piedrazul.api.citas.dto.CitaDTO;
import com.piedrazul.api.citas.dto.FranjaDTO;
import com.piedrazul.api.citas.exception.FranjaNoDisponibleException;
import com.piedrazul.api.citas.infrastructure.repository.CitaRepository;
import com.piedrazul.api.iam.repository.UserRepository;
import com.piedrazul.api.scheduling.domain.ConfiguracionAgenda;
import com.piedrazul.api.scheduling.domain.DiasAtencion;
import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.domain.FranjaHoraria;
import com.piedrazul.api.scheduling.domain.IntervaloCitas;
import com.piedrazul.api.scheduling.domain.VentanaAgendamiento;
import com.piedrazul.api.scheduling.repository.ConfiguracionAgendaRepository;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private ConfiguracionAgendaRepository configuracionAgendaRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    private CitaService service;

    private final String MEDICO_ID = "doc-1";
    private final LocalDate FECHA_FUTURA = LocalDate.now().plusDays(3);
    private final String PACIENTE_ID = "paciente@piedrazul.com";
    private final Doctor DOCTOR_ACTIVO = new Doctor(MEDICO_ID, "Dra. Ana Martinez", true);

    @BeforeEach
    void setUp() {
        service = new CitaService(citaRepository, configuracionAgendaRepository, doctorRepository, userRepository);
    }

    @Test
    void obtenerFranjasDisponibles_retornaFranjasConDisponibilidadCorrecta() {
        when(doctorRepository.findById(MEDICO_ID)).thenReturn(Optional.of(DOCTOR_ACTIVO));
        when(configuracionAgendaRepository.findByDoctorId(MEDICO_ID)).thenReturn(Optional.empty());

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
    void obtenerFranjasDisponibles_conConfiguracionPersonalizada_generaFranjasSegunConfiguracion() {
        when(doctorRepository.findById(MEDICO_ID)).thenReturn(Optional.of(DOCTOR_ACTIVO));

        ConfiguracionAgenda customConfig = new ConfiguracionAgenda(
                "cfg-1",
                MEDICO_ID,
                new VentanaAgendamiento(4),
                new DiasAtencion(Set.of(FECHA_FUTURA.getDayOfWeek())),
                new FranjaHoraria(LocalTime.of(9, 0), LocalTime.of(12, 0)),
                new IntervaloCitas(20)
        );
        when(configuracionAgendaRepository.findByDoctorId(MEDICO_ID)).thenReturn(Optional.of(customConfig));
        when(citaRepository.findByMedicoIdAndFecha(MEDICO_ID, FECHA_FUTURA)).thenReturn(Collections.emptyList());

        List<FranjaDTO> franjas = service.obtenerFranjasDisponibles(MEDICO_ID, FECHA_FUTURA);

        // De 9:00 a 12:00 con intervalos de 20 min son 9 franjas: 09:00, 09:20, 09:40, 10:00, 10:20, 10:40, 11:00, 11:20, 11:40
        assertThat(franjas).hasSize(9);
        assertThat(franjas.get(0).getHora()).isEqualTo(LocalTime.of(9, 0));
        assertThat(franjas.get(1).getHora()).isEqualTo(LocalTime.of(9, 20));
        assertThat(franjas.get(8).getHora()).isEqualTo(LocalTime.of(11, 40));
        assertThat(franjas).allMatch(FranjaDTO::isDisponible);
    }

    @Test
    void obtenerFranjasDisponibles_diaNoLaboralDelMedico_retornaListaVacia() {
        when(doctorRepository.findById(MEDICO_ID)).thenReturn(Optional.of(DOCTOR_ACTIVO));

        // Asignar un día diferente al día de la fecha consultada
        DayOfWeek otroDia = FECHA_FUTURA.getDayOfWeek() == DayOfWeek.MONDAY ? DayOfWeek.FRIDAY : DayOfWeek.MONDAY;
        ConfiguracionAgenda customConfig = new ConfiguracionAgenda(
                "cfg-1",
                MEDICO_ID,
                new VentanaAgendamiento(4),
                new DiasAtencion(Set.of(otroDia)),
                new FranjaHoraria(LocalTime.of(8, 0), LocalTime.of(12, 0)),
                new IntervaloCitas(30)
        );
        when(configuracionAgendaRepository.findByDoctorId(MEDICO_ID)).thenReturn(Optional.of(customConfig));

        List<FranjaDTO> franjas = service.obtenerFranjasDisponibles(MEDICO_ID, FECHA_FUTURA);

        assertThat(franjas).isEmpty();
    }

    @Test
    void obtenerFranjasDisponibles_fechaExcedeVentana_lanzaExcepcion() {
        when(doctorRepository.findById(MEDICO_ID)).thenReturn(Optional.of(DOCTOR_ACTIVO));

        ConfiguracionAgenda customConfig = new ConfiguracionAgenda(
                "cfg-1",
                MEDICO_ID,
                new VentanaAgendamiento(1), // Máximo 1 semana
                new DiasAtencion(Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)),
                new FranjaHoraria(LocalTime.of(8, 0), LocalTime.of(17, 0)),
                new IntervaloCitas(30)
        );
        when(configuracionAgendaRepository.findByDoctorId(MEDICO_ID)).thenReturn(Optional.of(customConfig));

        LocalDate fechaLejana = LocalDate.now().plusWeeks(2);

        assertThatThrownBy(() -> service.obtenerFranjasDisponibles(MEDICO_ID, fechaLejana))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("excede la ventana de agendamiento permitida");
    }

    @Test
    void obtenerFranjasDisponibles_fechaPasada_lanzaExcepcion() {
        LocalDate fechaPasada = LocalDate.now().minusDays(1);

        assertThatThrownBy(() -> service.obtenerFranjasDisponibles(MEDICO_ID, fechaPasada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se pueden consultar fechas pasadas");
    }

    @Test
    void obtenerFranjasDisponibles_medicoInexistenteOInactivo_lanzaExcepcion() {
        when(doctorRepository.findById("doc-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerFranjasDisponibles("doc-inexistente", FECHA_FUTURA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El medico/terapista seleccionado no esta disponible");
    }

    @Test
    void agendarCita_franjaDisponible_guardaYRetornaDTO() {
        LocalTime hora = LocalTime.of(9, 0);
        AgendarCitaRequest request = new AgendarCitaRequest(MEDICO_ID, FECHA_FUTURA, hora);

        when(doctorRepository.findById(MEDICO_ID)).thenReturn(Optional.of(DOCTOR_ACTIVO));
        when(configuracionAgendaRepository.findByDoctorId(MEDICO_ID)).thenReturn(Optional.empty());
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

        when(doctorRepository.findById(MEDICO_ID)).thenReturn(Optional.of(DOCTOR_ACTIVO));
        when(configuracionAgendaRepository.findByDoctorId(MEDICO_ID)).thenReturn(Optional.empty());
        when(citaRepository.existsByMedicoIdAndFechaAndHora(MEDICO_ID, FECHA_FUTURA, hora))
                .thenReturn(true);

        assertThatThrownBy(() -> service.agendarCita(request, PACIENTE_ID))
                .isInstanceOf(FranjaNoDisponibleException.class)
                .hasMessageContaining("ya está ocupada");
    }

    @Test
    void agendarCita_horaFueraDeFranjaHoraria_lanzaExcepcion() {
        LocalTime horaInvalida = LocalTime.of(7, 30); // Antes de las 8:00
        AgendarCitaRequest request = new AgendarCitaRequest(MEDICO_ID, FECHA_FUTURA, horaInvalida);

        when(doctorRepository.findById(MEDICO_ID)).thenReturn(Optional.of(DOCTOR_ACTIVO));
        when(configuracionAgendaRepository.findByDoctorId(MEDICO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.agendarCita(request, PACIENTE_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La hora debe estar entre");
    }

    @Test
    void agendarCita_horaNoAlineadaConIntervalo_lanzaExcepcion() {
        LocalTime horaDesalineada = LocalTime.of(8, 15); // Intervalo por defecto es 30 min (8:00, 8:30)
        AgendarCitaRequest request = new AgendarCitaRequest(MEDICO_ID, FECHA_FUTURA, horaDesalineada);

        when(doctorRepository.findById(MEDICO_ID)).thenReturn(Optional.of(DOCTOR_ACTIVO));
        when(configuracionAgendaRepository.findByDoctorId(MEDICO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.agendarCita(request, PACIENTE_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La hora seleccionada no corresponde a un intervalo valido");
    }

    @Test
    void agendarCita_diaNoLaboral_lanzaExcepcion() {
        DayOfWeek otroDia = FECHA_FUTURA.getDayOfWeek() == DayOfWeek.MONDAY ? DayOfWeek.FRIDAY : DayOfWeek.MONDAY;
        ConfiguracionAgenda customConfig = new ConfiguracionAgenda(
                "cfg-1",
                MEDICO_ID,
                new VentanaAgendamiento(4),
                new DiasAtencion(Set.of(otroDia)),
                new FranjaHoraria(LocalTime.of(8, 0), LocalTime.of(17, 0)),
                new IntervaloCitas(30)
        );

        when(doctorRepository.findById(MEDICO_ID)).thenReturn(Optional.of(DOCTOR_ACTIVO));
        when(configuracionAgendaRepository.findByDoctorId(MEDICO_ID)).thenReturn(Optional.of(customConfig));

        AgendarCitaRequest request = new AgendarCitaRequest(MEDICO_ID, FECHA_FUTURA, LocalTime.of(9, 0));

        assertThatThrownBy(() -> service.agendarCita(request, PACIENTE_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El medico no atiende en el dia seleccionado");
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
