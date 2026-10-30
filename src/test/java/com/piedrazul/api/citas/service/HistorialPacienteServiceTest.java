package com.piedrazul.api.citas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.piedrazul.api.citas.domain.Cedula;
import com.piedrazul.api.citas.domain.Cita;
import com.piedrazul.api.citas.domain.EstadoCita;
import com.piedrazul.api.citas.dto.CitaHistorialDTO;
import com.piedrazul.api.citas.dto.HistorialPacienteResponse;
import com.piedrazul.api.citas.infrastructure.repository.CitaRepository;
import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.repository.UserRepository;
import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

@ExtendWith(MockitoExtension.class)
class HistorialPacienteServiceTest {

    private static final String CEDULA = "1061000001";
    private static final String EMAIL = "ana.ruiz@gmail.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private DoctorRepository doctorRepository;

    private HistorialPacienteService service;
    private User paciente;

    @BeforeEach
    void setUp() {
        service = new HistorialPacienteService(userRepository, citaRepository, doctorRepository);
        paciente = new User("pac-1", "Ana Ruiz", EMAIL, CEDULA, "3001112233", "hash", RoleEnum.PATIENT);
    }

    @Test
    void consultarPorCedula_pacienteConCitas_retornaNombreCitasConNombreDelMedicoYTotal() {
        Cita reciente = new Cita(EMAIL, "doc-4", LocalDate.of(2026, 11, 7), LocalTime.of(14, 0), EstadoCita.CONFIRMADA);
        Cita antigua = new Cita(EMAIL, "doc-1", LocalDate.of(2026, 11, 1), LocalTime.of(8, 30), EstadoCita.PENDIENTE);
        when(userRepository.findByNumeroIdentificacion(CEDULA)).thenReturn(Optional.of(paciente));
        when(citaRepository.findByPacienteIdOrderByFechaDescHoraDesc(EMAIL)).thenReturn(List.of(reciente, antigua));
        when(doctorRepository.findAll()).thenReturn(List.of(
                new Doctor("doc-1", "Dra. Ana Martinez", true),
                new Doctor("doc-4", "Dra. Ibis Gonzalez", true)));

        HistorialPacienteResponse respuesta = service.consultarPorCedula(CEDULA);

        assertThat(respuesta.nombrePaciente()).isEqualTo("Ana Ruiz");
        assertThat(respuesta.total()).isEqualTo(2);
        assertThat(respuesta.citas())
                .extracting(CitaHistorialDTO::medicoNombre)
                .containsExactly("Dra. Ibis Gonzalez", "Dra. Ana Martinez");
        assertThat(respuesta.citas().get(0).fecha()).isEqualTo(LocalDate.of(2026, 11, 7));
    }

    @Test
    void consultarPorCedula_pacienteSinCitas_retornaNombreYTotalCero() {
        when(userRepository.findByNumeroIdentificacion(CEDULA)).thenReturn(Optional.of(paciente));
        when(citaRepository.findByPacienteIdOrderByFechaDescHoraDesc(EMAIL)).thenReturn(List.of());

        HistorialPacienteResponse respuesta = service.consultarPorCedula(CEDULA);

        assertThat(respuesta.nombrePaciente()).isEqualTo("Ana Ruiz");
        assertThat(respuesta.total()).isZero();
        assertThat(respuesta.citas()).isEmpty();
        verify(doctorRepository, never()).findAll();
    }

    @Test
    void consultarPorCedula_cedulaNoRegistrada_lanzaMensajePacienteNoEncontrado() {
        when(userRepository.findByNumeroIdentificacion("1061999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultarPorCedula("1061999999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(HistorialPacienteService.MENSAJE_PACIENTE_NO_ENCONTRADO);
    }

    @Test
    void consultarPorCedula_cedulaDeUsuarioQueNoEsPaciente_lanzaMensajePacienteNoEncontrado() {
        User medico = new User("user-2", "Dr. Ejemplo", "dr@piedrazul.com", "1061555555",
                "3000000000", "hash", RoleEnum.MEDICO);
        when(userRepository.findByNumeroIdentificacion("1061555555")).thenReturn(Optional.of(medico));

        assertThatThrownBy(() -> service.consultarPorCedula("1061555555"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(HistorialPacienteService.MENSAJE_PACIENTE_NO_ENCONTRADO);

        verify(citaRepository, never()).findByPacienteIdOrderByFechaDescHoraDesc(any());
    }

    @Test
    void consultarPorCedula_cedulaConFormatoInvalido_lanzaMensajeFormatoSinConsultar() {
        assertThatThrownBy(() -> service.consultarPorCedula("12ab"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Cedula.MENSAJE_FORMATO_INVALIDO);

        verify(userRepository, never()).findByNumeroIdentificacion(any());
    }

    @Test
    void consultarPorCedula_cedulaVacia_lanzaMensajeCedulaVacia() {
        assertThatThrownBy(() -> service.consultarPorCedula(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Cedula.MENSAJE_CEDULA_VACIA);

        verify(userRepository, never()).findByNumeroIdentificacion(any());
    }

    @Test
    void consultarPorCedula_citaConMedicoFueraDeLaTablaDoctores_muestraSuId() {
        Cita citaTerapista = new Cita(EMAIL, "ter-99", LocalDate.of(2026, 11, 3), LocalTime.of(9, 0), EstadoCita.PENDIENTE);
        when(userRepository.findByNumeroIdentificacion(CEDULA)).thenReturn(Optional.of(paciente));
        when(citaRepository.findByPacienteIdOrderByFechaDescHoraDesc(EMAIL)).thenReturn(List.of(citaTerapista));
        when(doctorRepository.findAll()).thenReturn(List.of(new Doctor("doc-1", "Dra. Ana Martinez", true)));

        HistorialPacienteResponse respuesta = service.consultarPorCedula(CEDULA);

        assertThat(respuesta.citas().get(0).medicoNombre()).isEqualTo("ter-99");
    }
}