package com.piedrazul.api.scheduling.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.piedrazul.api.scheduling.domain.Doctor;
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
}
