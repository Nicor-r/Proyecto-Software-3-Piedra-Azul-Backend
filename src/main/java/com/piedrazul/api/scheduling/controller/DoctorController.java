package com.piedrazul.api.scheduling.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.dto.DoctorRequest;
import com.piedrazul.api.scheduling.dto.DoctorResponse;
import com.piedrazul.api.scheduling.mapper.DoctorMapper;
import com.piedrazul.api.scheduling.service.DoctorService;

import jakarta.validation.Valid;

/**
 * @file DoctorController.java
 * @brief Controlador REST para la gestión de doctores.
 */
@RestController
@RequestMapping({"/api/admin/doctors", "/api/doctores"})
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorMapper doctorMapper;

    public DoctorController(DoctorService doctorService, DoctorMapper doctorMapper) {
        this.doctorService = doctorService;
        this.doctorMapper = doctorMapper;
    }

    /**
     * @brief Lista todos los doctores registrados.
     */
    @GetMapping
    public List<DoctorResponse> listar() {
        return doctorService.listarDoctores().stream()
                .map(doctorMapper::toResponse)
                .toList();
    }

    /**
     * @brief Obtiene el detalle de un doctor por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> obtenerPorId(@PathVariable String id) {
        Doctor doctor = doctorService.obtenerDoctorPorId(id);
        return ResponseEntity.ok(doctorMapper.toResponse(doctor));
    }

    /**
     * @brief Registra un nuevo médico o terapista (requiere rol ADMIN si se invoca por /api/admin/doctors).
     */
    @PostMapping
    public ResponseEntity<DoctorResponse> crearDoctor(@RequestBody @Valid DoctorRequest request) {
        Doctor nuevo = doctorService.registrarDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorMapper.toResponse(nuevo));
    }

    /**
     * @brief Actualiza los datos o estado de un médico o terapista.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> actualizarDoctor(@PathVariable String id,
                                                          @RequestBody @Valid DoctorRequest request) {
        Doctor actualizado = doctorService.actualizarDoctor(id, request);
        return ResponseEntity.ok(doctorMapper.toResponse(actualizado));
    }
}