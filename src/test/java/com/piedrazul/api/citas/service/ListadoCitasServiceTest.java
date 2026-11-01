package com.piedrazul.api.citas.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.piedrazul.api.citas.domain.Cita;
import com.piedrazul.api.citas.domain.EstadoCita;
import com.piedrazul.api.citas.dto.ListadoCitasResponse;
import com.piedrazul.api.citas.infrastructure.repository.CitaRepository;
import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.infrastructure.repository.SpringDataTerapistaRepository;
import com.piedrazul.api.iam.repository.UserRepository;
import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

@ExtendWith(MockitoExtension.class)
class ListadoCitasServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;
    @Mock
    private SpringDataTerapistaRepository terapistaRepository;
    private ListadoCitasService service;

    private final String MEDICO_ID = "doc-1";
    private final LocalDate FECHA_FUTURA = LocalDate.now().plusDays(2);
    private final Doctor DOCTOR_ACTIVO = new Doctor(MEDICO_ID, "Dra. Ana Martinez", true);
    private final Doctor DOCTOR_INACTIVO = new Doctor("doc-inactivo", "Dr. Inactivo", false);

    @BeforeEach
    void setUp() {
        service = new ListadoCitasService(citaRepository, doctorRepository, userRepository, terapistaRepository);
    }

    @Test
    void listarCitas_conMedicoYFechaValidos_retornaListadoYTotal() {
        when(doctorRepository.findById(MEDICO_ID)).thenReturn(Optional.of(DOCTOR_ACTIVO));
        Cita c1 = new Cita("pac1@gmail.com", MEDICO_ID, FECHA_FUTURA, LocalTime.of(8, 0), EstadoCita.CONFIRMADA);
        c1.setId(1L);
        when(citaRepository.findByMedicoIdAndFechaOrderByHoraAsc(MEDICO_ID, FECHA_FUTURA))
                .thenReturn(List.of(c1));
        when(userRepository.findByEmail("pac1@gmail.com")).thenReturn(Optional.of(
                new User("u-1", "Ana Ruiz", "pac1@gmail.com", "123", "456", "pwd", RoleEnum.PATIENT)));

        ListadoCitasResponse resp = service.listarCitas(MEDICO_ID, FECHA_FUTURA, "TODOS");

        assertThat(resp.getTotal()).isEqualTo(1);
        assertThat(resp.getCitas()).hasSize(1);
        assertThat(resp.getCitas().get(0).getPacienteNombre()).isEqualTo("Ana Ruiz");
        assertThat(resp.getCitas().get(0).getMedicoNombre()).isEqualTo("Dra. Ana Martinez");
    }

    @Test
    void listarCitas_sinResultados_retornaTotalCeroYListaVacia() {
        when(doctorRepository.findById(MEDICO_ID)).thenReturn(Optional.of(DOCTOR_ACTIVO));
        when(citaRepository.findByMedicoIdAndFechaOrderByHoraAsc(MEDICO_ID, FECHA_FUTURA))
                .thenReturn(Collections.emptyList());

        ListadoCitasResponse resp = service.listarCitas(MEDICO_ID, FECHA_FUTURA, null);

        assertThat(resp.getTotal()).isEqualTo(0);
        assertThat(resp.getCitas()).isEmpty();
    }

    @Test
    void listarCitas_conSoloMedico_retornaCitasDelMedico() {
        when(doctorRepository.findById(MEDICO_ID)).thenReturn(Optional.of(DOCTOR_ACTIVO));
        Cita c1 = new Cita("pac1@gmail.com", MEDICO_ID, FECHA_FUTURA, LocalTime.of(8, 0), EstadoCita.CONFIRMADA);
        c1.setId(1L);
        when(citaRepository.findByMedicoIdOrderByFechaAscHoraAsc(MEDICO_ID))
                .thenReturn(List.of(c1));
        when(userRepository.findByEmail("pac1@gmail.com")).thenReturn(Optional.of(
                new User("u-1", "Ana Ruiz", "pac1@gmail.com", "123", "456", "pwd", RoleEnum.PATIENT)));

        ListadoCitasResponse resp = service.listarCitas(MEDICO_ID, null, null);

        assertThat(resp.getTotal()).isEqualTo(1);
        assertThat(resp.getCitas()).hasSize(1);
        assertThat(resp.getCitas().get(0).getMedicoNombre()).isEqualTo("Dra. Ana Martinez");
    }

    @Test
    void listarCitas_conSoloFecha_retornaCitasDeLaFecha() {
        Cita c1 = new Cita("pac1@gmail.com", MEDICO_ID, FECHA_FUTURA, LocalTime.of(8, 0), EstadoCita.CONFIRMADA);
        c1.setId(1L);
        when(doctorRepository.findById(MEDICO_ID)).thenReturn(Optional.of(DOCTOR_ACTIVO));
        when(citaRepository.findByFechaOrderByHoraAsc(FECHA_FUTURA))
                .thenReturn(List.of(c1));
        when(userRepository.findByEmail("pac1@gmail.com")).thenReturn(Optional.of(
                new User("u-1", "Ana Ruiz", "pac1@gmail.com", "123", "456", "pwd", RoleEnum.PATIENT)));

        ListadoCitasResponse resp = service.listarCitas(null, FECHA_FUTURA, null);

        assertThat(resp.getTotal()).isEqualTo(1);
        assertThat(resp.getCitas()).hasSize(1);
    }

    @Test
    void listarCitas_fechaAnterior_lanzaExcepcion() {
        LocalDate fechaPasada = LocalDate.now().minusDays(1);

        assertThatThrownBy(() -> service.listarCitas(MEDICO_ID, fechaPasada, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La fecha ingresada no es valida");
    }

    @Test
    void listarCitas_medicoInexistenteOInactivo_lanzaExcepcion() {
        when(doctorRepository.findById("doc-no-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listarCitas("doc-no-existe", FECHA_FUTURA, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El medico/terapista seleccionado no esta disponible");

        when(doctorRepository.findById("doc-inactivo")).thenReturn(Optional.of(DOCTOR_INACTIVO));

        assertThatThrownBy(() -> service.listarCitas("doc-inactivo", FECHA_FUTURA, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El medico/terapista seleccionado no esta disponible");
    }

    @Test
    void listarCitas_conFiltroEstado_filtraPorEstado() {
        when(doctorRepository.findById(MEDICO_ID)).thenReturn(Optional.of(DOCTOR_ACTIVO));
        Cita c1 = new Cita("pac1@gmail.com", MEDICO_ID, FECHA_FUTURA, LocalTime.of(8, 0), EstadoCita.PENDIENTE);
        c1.setId(1L);
        when(citaRepository.findByMedicoIdAndFechaAndEstadoOrderByHoraAsc(MEDICO_ID, FECHA_FUTURA, EstadoCita.PENDIENTE))
                .thenReturn(List.of(c1));

        ListadoCitasResponse resp = service.listarCitas(MEDICO_ID, FECHA_FUTURA, "PENDIENTE");

        assertThat(resp.getTotal()).isEqualTo(1);
        assertThat(resp.getCitas().get(0).getEstado()).isEqualTo(EstadoCita.PENDIENTE);
    }

    @Test
    void listarCitas_sinFiltros_retornaTodasLasCitas() {
        Cita c1 = new Cita("p1@gmail.com", "doc-1", FECHA_FUTURA, LocalTime.of(8, 0), EstadoCita.CONFIRMADA);
        Cita c2 = new Cita("p2@gmail.com", "doc-2", FECHA_FUTURA, LocalTime.of(9, 0), EstadoCita.CANCELADA);
        when(citaRepository.findAll()).thenReturn(List.of(c1, c2));

        ListadoCitasResponse resp = service.listarCitas(null, null, null);

        assertThat(resp.getTotal()).isEqualTo(2);
        assertThat(resp.getCitas()).hasSize(2);
    }
}
