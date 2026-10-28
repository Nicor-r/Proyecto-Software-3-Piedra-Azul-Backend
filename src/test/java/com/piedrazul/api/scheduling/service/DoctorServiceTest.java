package com.piedrazul.api.scheduling.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.dto.DoctorRequest;
import com.piedrazul.api.scheduling.exception.SchedulingException;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    private DoctorService service;

    @BeforeEach
    void setUp() {
        service = new DoctorService(doctorRepository);
    }

    @Test
    void listarDoctores_devuelveLosDoctoresDelRepositorio() {
        List<Doctor> doctores = List.of(
                new Doctor("doc-1", "Dra. Ana Martinez", true),
                new Doctor("doc-3", "Dra. Laura Gomez", false)
        );
        when(doctorRepository.findAll()).thenReturn(doctores);

        List<Doctor> resultado = service.listarDoctores();

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Doctor::getId).containsExactly("doc-1", "doc-3");
    }

    @Test
    void obtenerDoctorPorId_existente_retornaDoctor() {
        Doctor doctor = new Doctor("doc-1", "Dra. Ana Martinez", true);
        when(doctorRepository.findById("doc-1")).thenReturn(Optional.of(doctor));

        Doctor resultado = service.obtenerDoctorPorId("doc-1");

        assertThat(resultado.getId()).isEqualTo("doc-1");
        assertThat(resultado.getNombre()).isEqualTo("Dra. Ana Martinez");
    }

    @Test
    void obtenerDoctorPorId_noExiste_lanzaExcepcion() {
        when(doctorRepository.findById("doc-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerDoctorPorId("doc-inexistente"))
                .isInstanceOf(SchedulingException.class)
                .hasMessage("El medico/terapista no existe");
    }

    @Test
    void registrarDoctor_datosValidos_guardaYRetornaDoctor() {
        DoctorRequest request = new DoctorRequest("Dr. Nuevo Especialista", true);
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> {
            Doctor d = invocation.getArgument(0);
            d.setId("doc-nuevo");
            return d;
        });

        Doctor guardado = service.registrarDoctor(request);

        assertThat(guardado.getId()).isEqualTo("doc-nuevo");
        assertThat(guardado.getNombre()).isEqualTo("Dr. Nuevo Especialista");
        assertThat(guardado.isActivo()).isTrue();
    }

    @Test
    void registrarDoctor_nombreVacio_lanzaExcepcion() {
        DoctorRequest request = new DoctorRequest("", true);

        assertThatThrownBy(() -> service.registrarDoctor(request))
                .isInstanceOf(SchedulingException.class)
                .hasMessage("El nombre del medico/terapista es obligatorio");
    }

    @Test
    void actualizarDoctor_datosValidos_actualizaDoctor() {
        Doctor doctorExistente = new Doctor("doc-1", "Nombre Antiguo", true);
        when(doctorRepository.findById("doc-1")).thenReturn(Optional.of(doctorExistente));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

        DoctorRequest request = new DoctorRequest("Nombre Actualizado", false);
        Doctor resultado = service.actualizarDoctor("doc-1", request);

        assertThat(resultado.getNombre()).isEqualTo("Nombre Actualizado");
        assertThat(resultado.isActivo()).isFalse();

        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Nombre Actualizado");
        assertThat(captor.getValue().isActivo()).isFalse();
    }

    @Test
    void actualizarDoctor_noExiste_lanzaExcepcion() {
        when(doctorRepository.findById("doc-no-existe")).thenReturn(Optional.empty());

        DoctorRequest request = new DoctorRequest("Nombre", true);

        assertThatThrownBy(() -> service.actualizarDoctor("doc-no-existe", request))
                .isInstanceOf(SchedulingException.class)
                .hasMessage("El medico/terapista no existe");
    }
}
