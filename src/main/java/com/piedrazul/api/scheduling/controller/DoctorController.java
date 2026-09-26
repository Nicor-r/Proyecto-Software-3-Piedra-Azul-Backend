package com.piedrazul.api.scheduling.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.piedrazul.api.scheduling.dto.DoctorResponse;
import com.piedrazul.api.scheduling.mapper.DoctorMapper;
import com.piedrazul.api.scheduling.service.DoctorService;

/**
 * Endpoint de solo lectura para que el frontend pueda llenar el selector de
 * medico/terapista. Sin CRUD este corte (ver SeedDoctorRepository).
 */
@RestController
@RequestMapping("/api/admin/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorMapper doctorMapper;

    public DoctorController(DoctorService doctorService, DoctorMapper doctorMapper) {
        this.doctorService = doctorService;
        this.doctorMapper = doctorMapper;
    }

    @GetMapping
    public List<DoctorResponse> listar() {
        return doctorService.listarDoctores().stream()
                .map(doctorMapper::toResponse)
                .toList();
    }
}
