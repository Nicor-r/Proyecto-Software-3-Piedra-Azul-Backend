package com.piedrazul.api.scheduling.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.piedrazul.api.scheduling.dto.DoctorResponse;
import com.piedrazul.api.scheduling.mapper.DoctorMapper;
import com.piedrazul.api.scheduling.service.DoctorService;

/**
 * @file DoctorController.java
 * @brief Controlador REST de solo lectura para la gestión de doctores.
 *
 * @details
 * Expone el listado de doctores para que el frontend pueda llenar el selector
 * de médico o terapista. En este corte no se implementa CRUD; los datos
 * provienen de {@code SeedDoctorRepository}.
 *
 * Endpoint principal:
 * - {@code GET /api/admin/doctors}: retorna la lista de doctores disponibles.
 */
@RestController
@RequestMapping("/api/admin/doctors")
public class DoctorController {

    /**
     * @brief Servicio encargado de la lógica de negocio relacionada con doctores.
     */
    private final DoctorService doctorService;

    /**
     * @brief Mapper encargado de convertir las entidades de doctor a DTOs de respuesta.
     */
    private final DoctorMapper doctorMapper;

    /**
     * @brief Construye el controlador con sus dependencias.
     *
     * @param doctorService Servicio de doctores.
     * @param doctorMapper  Mapper de doctores.
     */
    public DoctorController(DoctorService doctorService, DoctorMapper doctorMapper) {
        this.doctorService = doctorService;
        this.doctorMapper = doctorMapper;
    }

    /**
     * @brief Lista todos los doctores disponibles.
     *
     * @details
     * Obtiene los doctores desde el servicio, los transforma a DTOs de respuesta
     * mediante el mapper y retorna la lista resultante.
     *
     * @return Lista de {@link DoctorResponse} con los doctores registrados.
     */
    @GetMapping
    public List<DoctorResponse> listar() {
        return doctorService.listarDoctores().stream()
                .map(doctorMapper::toResponse)
                .toList();
    }
}