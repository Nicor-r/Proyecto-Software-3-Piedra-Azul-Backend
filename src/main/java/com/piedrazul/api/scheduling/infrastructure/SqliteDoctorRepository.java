package com.piedrazul.api.scheduling.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.mapper.DoctorEntityMapper;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

/**
 * @file SqliteDoctorRepository.java
 * @brief Implementación de {@link DoctorRepository} respaldada en SQLite vía Spring Data JPA.
 */
@Repository
public class SqliteDoctorRepository implements DoctorRepository {

    private final SpringDataDoctorRepository springDataRepository;
    private final DoctorEntityMapper mapper;

    public SqliteDoctorRepository(SpringDataDoctorRepository springDataRepository,
                                  DoctorEntityMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Doctor> findById(String id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Doctor> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Doctor save(Doctor doctor) {
        if (doctor.getId() == null || doctor.getId().isBlank()) {
            doctor.setId("doc-" + UUID.randomUUID().toString().substring(0, 8));
        }
        DoctorJpaEntity entity = mapper.toEntity(doctor);
        DoctorJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Doctor> saveAll(List<Doctor> doctores) {
        List<DoctorJpaEntity> entities = doctores.stream()
                .map(mapper::toEntity)
                .toList();
        return springDataRepository.saveAll(entities).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }

    @Override
    public boolean existsById(String id) {
        return springDataRepository.existsById(id);
    }
}
