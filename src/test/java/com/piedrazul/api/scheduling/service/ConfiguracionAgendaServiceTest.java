package com.piedrazul.api.scheduling.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.piedrazul.api.scheduling.domain.ConfiguracionAgenda;
import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.dto.ConfiguracionAgendaRequest;
import com.piedrazul.api.scheduling.exception.SchedulingException;
import com.piedrazul.api.scheduling.repository.ConfiguracionAgendaRepository;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

@ExtendWith(MockitoExtension.class)
class ConfiguracionAgendaServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private ConfiguracionAgendaRepository configuracionAgendaRepository;

    private ConfiguracionAgendaService service;

    @BeforeEach
    void setUp() {
        service = new ConfiguracionAgendaService(doctorRepository, configuracionAgendaRepository);
    }

    private ConfiguracionAgendaRequest requestValido() {
        ConfiguracionAgendaRequest request = new ConfiguracionAgendaRequest();
        request.setDoctorId("doc-1");
        request.setVentanaSemanas(4);
        request.setDiasAtencion(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        request.setHoraInicio(LocalTime.of(8, 0));
        request.setHoraFin(LocalTime.of(17, 0));
        request.setIntervaloMinutos(30);
        return request;
    }

    @Test
    void configuracionValida_doctorActivo_guardaCorrectamente() {
        Doctor doctorActivo = new Doctor("doc-1", "Dra. Ana Martinez", true);
        when(doctorRepository.findById("doc-1")).thenReturn(Optional.of(doctorActivo));
        when(configuracionAgendaRepository.findByDoctorId("doc-1")).thenReturn(Optional.empty());
        when(configuracionAgendaRepository.save(any(ConfiguracionAgenda.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConfiguracionAgenda resultado = service.configurarAgenda(requestValido());

        assertThat(resultado.getDoctorId()).isEqualTo("doc-1");
        assertThat(resultado.getVentanaAgendamiento().getSemanas()).isEqualTo(4);
        verify(configuracionAgendaRepository, times(1)).save(any(ConfiguracionAgenda.class));
    }

    @Test
    void doctorInexistente_lanzaSchedulingException_noGuarda() {
        when(doctorRepository.findById("doc-x")).thenReturn(Optional.empty());

        ConfiguracionAgendaRequest request = requestValido();
        request.setDoctorId("doc-x");

        SchedulingException exception = assertThrows(SchedulingException.class,
                () -> service.configurarAgenda(request));

        assertThat(exception.getMessage()).isEqualTo("Debe seleccionar un medico/terapista valido");
        verify(configuracionAgendaRepository, never()).save(any(ConfiguracionAgenda.class));
    }

    @Test
    void doctorInactivo_lanzaSchedulingException_noGuarda() {
        Doctor doctorInactivo = new Doctor("doc-3", "Dra. Laura Gomez", false);
        when(doctorRepository.findById("doc-3")).thenReturn(Optional.of(doctorInactivo));

        ConfiguracionAgendaRequest request = requestValido();
        request.setDoctorId("doc-3");

        SchedulingException exception = assertThrows(SchedulingException.class,
                () -> service.configurarAgenda(request));

        assertThat(exception.getMessage()).isEqualTo("Debe seleccionar un medico/terapista valido");
        verify(configuracionAgendaRepository, never()).save(any(ConfiguracionAgenda.class));
    }

    @Test
    void ventanaSemanasNula_lanzaSchedulingException_noGuarda() {
        Doctor doctorActivo = new Doctor("doc-1", "Dra. Ana Martinez", true);
        when(doctorRepository.findById("doc-1")).thenReturn(Optional.of(doctorActivo));

        ConfiguracionAgendaRequest request = requestValido();
        request.setVentanaSemanas(null);

        assertThrows(SchedulingException.class, () -> service.configurarAgenda(request));
        verify(configuracionAgendaRepository, never()).save(any(ConfiguracionAgenda.class));
    }

    @Test
    void ventanaSemanasInvalida_propagaExcepcionDelValueObject() {
        Doctor doctorActivo = new Doctor("doc-1", "Dra. Ana Martinez", true);
        when(doctorRepository.findById("doc-1")).thenReturn(Optional.of(doctorActivo));

        ConfiguracionAgendaRequest request = requestValido();
        request.setVentanaSemanas(0);

        SchedulingException exception = assertThrows(SchedulingException.class,
                () -> service.configurarAgenda(request));

        assertThat(exception.getMessage())
                .isEqualTo("La ventana de agendamiento debe ser un numero entero mayor a 0");
    }

    @Test
    void franjaHorariaInvalida_propagaExcepcionDelValueObject() {
        Doctor doctorActivo = new Doctor("doc-1", "Dra. Ana Martinez", true);
        when(doctorRepository.findById("doc-1")).thenReturn(Optional.of(doctorActivo));

        ConfiguracionAgendaRequest request = requestValido();
        request.setHoraInicio(LocalTime.of(18, 0));
        request.setHoraFin(LocalTime.of(8, 0));

        assertThrows(SchedulingException.class, () -> service.configurarAgenda(request));
        verify(configuracionAgendaRepository, never()).save(any(ConfiguracionAgenda.class));
    }

    @Test
    void intervaloMinutosNulo_lanzaSchedulingException_noGuarda() {
        Doctor doctorActivo = new Doctor("doc-1", "Dra. Ana Martinez", true);
        when(doctorRepository.findById("doc-1")).thenReturn(Optional.of(doctorActivo));

        ConfiguracionAgendaRequest request = requestValido();
        request.setIntervaloMinutos(null);

        assertThrows(SchedulingException.class, () -> service.configurarAgenda(request));
        verify(configuracionAgendaRepository, never()).save(any(ConfiguracionAgenda.class));
    }

    @Test
    void doctorYaTieneConfiguracion_actualizaMismoId() {
        Doctor doctorActivo = new Doctor("doc-1", "Dra. Ana Martinez", true);
        when(doctorRepository.findById("doc-1")).thenReturn(Optional.of(doctorActivo));

        ConfiguracionAgenda configuracionExistente = new ConfiguracionAgenda();
        configuracionExistente.setId("config-existente-id");
        when(configuracionAgendaRepository.findByDoctorId("doc-1"))
                .thenReturn(Optional.of(configuracionExistente));
        when(configuracionAgendaRepository.save(any(ConfiguracionAgenda.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.configurarAgenda(requestValido());

        ArgumentCaptor<ConfiguracionAgenda> captor = ArgumentCaptor.forClass(ConfiguracionAgenda.class);
        verify(configuracionAgendaRepository).save(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo("config-existente-id");
    }

    @Test
    void diasAtencionNulo_seTrataComoVacio_lanzaSchedulingException() {
        Doctor doctorActivo = new Doctor("doc-1", "Dra. Ana Martinez", true);
        when(doctorRepository.findById("doc-1")).thenReturn(Optional.of(doctorActivo));

        ConfiguracionAgendaRequest request = requestValido();
        request.setDiasAtencion(null);

        assertThrows(SchedulingException.class, () -> service.configurarAgenda(request));
    }
}
