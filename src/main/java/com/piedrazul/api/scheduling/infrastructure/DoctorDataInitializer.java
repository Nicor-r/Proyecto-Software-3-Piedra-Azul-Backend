package com.piedrazul.api.scheduling.infrastructure;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

/**
 * @file DoctorDataInitializer.java
 * @brief Inicializador que siembra los médicos iniciales en SQLite si la tabla está vacía.
 */
@Component
@Order(1)
public class DoctorDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DoctorDataInitializer.class);

    private final DoctorRepository doctorRepository;

    public DoctorDataInitializer(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (doctorRepository.count() > 0) {
            return;
        }

        List<Doctor> doctores = List.of(
                new Doctor("doc-1", "Dra. Ana Martinez - Medicina General", true),
                new Doctor("doc-2", "Dr. Carlos Ruiz - Fisioterapia", true),
                new Doctor("doc-3", "Dra. Laura Gomez - Odontologia (inactiva)", false),
                new Doctor("doc-4", "Dra. Ibis González - Cardiología", true),
                new Doctor("doc-5", "Dra. Clara Inés - Endocrinología", true),
                new Doctor("doc-6", "Dr. Rocha - Terapia Física", true),
                new Doctor("doc-7", "Dra. Pastrana - Pediatría", true)
        );

        doctorRepository.saveAll(doctores);
        log.info("Sembrados {} doctores iniciales en la base de datos", doctores.size());
    }
}
