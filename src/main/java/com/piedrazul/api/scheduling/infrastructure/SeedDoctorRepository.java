package com.piedrazul.api.scheduling.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

/**
 * @file SeedDoctorRepository.java
 * @brief Implementación de {@link DoctorRepository} con datos en memoria.
 *
 * @details
 * Sustituye temporalmente a un repositorio persistente mientras no existe CRUD
 * de médicos. Los datos son fijos (definidos en {@code DOCTORES}) y no se
 * modifican en tiempo de ejecución, por lo que {@link #findAll()} devuelve la
 * misma lista en cada llamada.
 */
@Repository
public class SeedDoctorRepository implements DoctorRepository {

    private static final List<Doctor> DOCTORES = List.of(
            new Doctor("doc-1", "Dra. Ana Martinez - Medicina General", true),
            new Doctor("doc-2", "Dr. Carlos Ruiz - Fisioterapia", true),
            new Doctor("doc-3", "Dra. Laura Gomez - Odontologia (inactiva)", false)
    );

    /**
     * @brief Busca un doctor por su identificador.
     *
     * @param id Identificador del doctor.
     * @return {@link Optional} con el doctor si existe, o vacío en caso contrario.
     */
    @Override
    public Optional<Doctor> findById(String id) {
        return DOCTORES.stream()
                .filter(doctor -> doctor.getId().equals(id))
                .findFirst();
    }

    /**
     * @brief Retorna todos los doctores disponibles.
     *
     * @return Lista con los doctores sembrados.
     */
    @Override
    public List<Doctor> findAll() {
        return DOCTORES;
    }
}