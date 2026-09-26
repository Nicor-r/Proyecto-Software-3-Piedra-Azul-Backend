package com.piedrazul.api.scheduling.repository;

import java.util.List;
import java.util.Optional;

import com.piedrazul.api.scheduling.domain.Doctor;

/**
 * @file DoctorRepository.java
 * @brief Puerto de persistencia para {@link Doctor}.
 *
 * @details
 * Define el contrato de acceso a doctores. Al vivir en el dominio, desacopla
 * a los servicios de la tecnología concreta de almacenamiento; en este corte
 * la única implementación es {@code SeedDoctorRepository}, con datos en
 * memoria.
 */
public interface DoctorRepository {

    /**
     * @brief Busca un doctor por su identificador.
     *
     * @param id Identificador del doctor.
     * @return {@link Optional} con el doctor si existe, o vacío en caso contrario.
     */
    Optional<Doctor> findById(String id);

    /**
     * @brief Retorna todos los doctores disponibles.
     *
     * @return Lista de doctores.
     */
    List<Doctor> findAll();
}