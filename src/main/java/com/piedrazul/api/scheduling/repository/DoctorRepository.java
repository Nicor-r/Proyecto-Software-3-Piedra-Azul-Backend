package com.piedrazul.api.scheduling.repository;

import java.util.List;
import java.util.Optional;

import com.piedrazul.api.scheduling.domain.Doctor;

/**
 * @file DoctorRepository.java
 * @brief Puerto de persistencia para {@link Doctor}.
 *
 * @details
 *          Define el contrato de acceso y persistencia para doctores. Desacopla
 *          los servicios de la tecnología concreta de almacenamiento (SQLite
 *          vía JPA).
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
     * @brief Busca el doctor vinculado a una cuenta de usuario (modulo iam).
     *
     * @param userId Id del User con el que el medico inicia sesion.
     * @return {@link Optional} con el doctor si existe una ficha vinculada a ese
     *         usuario.
     */
    Optional<Doctor> findByUserId(String userId);

    /**
     * @brief Retorna todos los doctores disponibles.
     *
     * @return Lista de doctores.
     */
    List<Doctor> findAll();

    /**
     * @brief Guarda o actualiza un doctor.
     *
     * @param doctor Instancia del doctor a persistir.
     * @return El doctor persistido.
     */
    Doctor save(Doctor doctor);

    /**
     * @brief Guarda una lista de doctores.
     *
     * @param doctores Lista de doctores a persistir.
     * @return Lista de doctores persistidos.
     */
    List<Doctor> saveAll(List<Doctor> doctores);

    /**
     * @brief Cuenta la cantidad total de doctores registrados.
     *
     * @return Número total de doctores.
     */
    long count();

    /**
     * @brief Comprueba si existe un doctor con el id especificado.
     *
     * @param id Identificador del doctor.
     * @return {@code true} si existe; {@code false} de lo contrario.
     */
    boolean existsById(String id);
}