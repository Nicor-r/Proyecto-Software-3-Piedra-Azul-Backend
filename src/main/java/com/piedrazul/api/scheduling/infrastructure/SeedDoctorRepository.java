package com.piedrazul.api.scheduling.infrastructure;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

@Repository
public class SeedDoctorRepository implements DoctorRepository {

    private static final Map<String, Doctor> DOCTORES = Map.of(
            "doc-1", new Doctor("doc-1", "Dra. Ana Martinez - Medicina General", true),
            "doc-2", new Doctor("doc-2", "Dr. Carlos Ruiz - Fisioterapia", true),
            "doc-3", new Doctor("doc-3", "Dra. Laura Gomez - Odontologia (inactiva)", false));

    @Override
    public Optional<Doctor> findById(String id) {
        return Optional.ofNullable(DOCTORES.get(id));
    }

}
