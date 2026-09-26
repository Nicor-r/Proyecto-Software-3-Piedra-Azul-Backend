package com.piedrazul.api.scheduling.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

@Repository
public class SeedDoctorRepository implements DoctorRepository {

    private static final List<Doctor> DOCTORES = List.of(
            new Doctor("doc-1", "Dra. Ana Martinez - Medicina General", true),
            new Doctor("doc-2", "Dr. Carlos Ruiz - Fisioterapia", true),
            new Doctor("doc-3", "Dra. Laura Gomez - Odontologia (inactiva)", false)
    );

    @Override
    public Optional<Doctor> findById(String id) {
        return DOCTORES.stream()
                .filter(doctor -> doctor.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Doctor> findAll() {
        return DOCTORES;
    }
}
