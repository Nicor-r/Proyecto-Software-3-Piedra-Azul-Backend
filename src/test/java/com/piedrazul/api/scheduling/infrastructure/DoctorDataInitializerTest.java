package com.piedrazul.api.scheduling.infrastructure;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.piedrazul.api.scheduling.repository.DoctorRepository;

@ExtendWith(MockitoExtension.class)
class DoctorDataInitializerTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorDataInitializer initializer;

    @Test
    void run_siNoHayDoctores_siembraDoctoresIniciales() {
        when(doctorRepository.count()).thenReturn(0L);

        initializer.run(null);

        verify(doctorRepository).saveAll(anyList());
    }

    @Test
    void run_siYaHayDoctores_noHaceNada() {
        when(doctorRepository.count()).thenReturn(7L);

        initializer.run(null);

        verify(doctorRepository, never()).saveAll(anyList());
    }
}
