package com.piedrazul.api.scheduling.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

/**
 * @file DoctorService.java
 * @brief Servicio que orquesta las consultas de médicos y terapistas.
 *
 * @details
 * Actualmente actúa como paso directo hacia {@link DoctorRepository}. Se
 * mantiene como capa de servicio —en lugar de que el controlador invoque al
 * repositorio directamente— para dejar el camino abierto a incorporar reglas
 * de negocio más adelante (por ejemplo, filtrar solo médicos activos).
 */
@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    /**
     * @brief Construye el servicio con su repositorio.
     *
     * @param doctorRepository Repositorio de doctores.
     */
    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    /**
     * @brief Lista todos los doctores disponibles.
     *
     * @return Lista de doctores.
     */
    public List<Doctor> listarDoctores() {
        return doctorRepository.findAll();
    }
}