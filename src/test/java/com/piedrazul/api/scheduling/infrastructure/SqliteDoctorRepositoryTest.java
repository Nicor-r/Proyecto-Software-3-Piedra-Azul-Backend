package com.piedrazul.api.scheduling.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.mapper.DoctorEntityMapper;

@ExtendWith(MockitoExtension.class)
class SqliteDoctorRepositoryTest {

    @Mock
    private SpringDataDoctorRepository springDataRepository;

    private DoctorEntityMapper mapper;
    private SqliteDoctorRepository repository;

    @BeforeEach
    void setUp() {
        mapper = new DoctorEntityMapper();
        repository = new SqliteDoctorRepository(springDataRepository, mapper);
    }

    @Test
    void findById_encuentraDoctor_retornaDominio() {
        DoctorJpaEntity entity = new DoctorJpaEntity("doc-1", "Dra. Ana", true);
        when(springDataRepository.findById("doc-1")).thenReturn(Optional.of(entity));

        Optional<Doctor> result = repository.findById("doc-1");

        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Dra. Ana");
    }

    @Test
    void findAll_retornaListaConvertida() {
        DoctorJpaEntity e1 = new DoctorJpaEntity("doc-1", "Dra. Ana", true);
        DoctorJpaEntity e2 = new DoctorJpaEntity("doc-2", "Dr. Carlos", true);
        when(springDataRepository.findAll()).thenReturn(List.of(e1, e2));

        List<Doctor> result = repository.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void save_conIdNulo_generaIdYGuarda() {
        Doctor doctor = new Doctor(null, "Dra. Nueva", true);
        when(springDataRepository.save(any(DoctorJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Doctor saved = repository.save(doctor);

        assertThat(saved.getId()).startsWith("doc-");
        assertThat(saved.getNombre()).isEqualTo("Dra. Nueva");
        verify(springDataRepository).save(any(DoctorJpaEntity.class));
    }

    @Test
    void count_delegaEnSpringData() {
        when(springDataRepository.count()).thenReturn(5L);

        assertThat(repository.count()).isEqualTo(5L);
    }
}
