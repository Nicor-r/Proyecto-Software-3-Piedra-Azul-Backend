package com.piedrazul.api.scheduling.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.dto.DoctorRequest;
import com.piedrazul.api.scheduling.exception.SchedulingException;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

/**
 * @file DoctorService.java
 * @brief Servicio que orquesta la lógica de negocio para la gestión de médicos y terapistas.
 */
@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public List<Doctor> listarDoctores() {
        return doctorRepository.findAll();
    }

    public Doctor obtenerDoctorPorId(String id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new SchedulingException("El medico/terapista no existe"));
    }

    public Doctor registrarDoctor(DoctorRequest request) {
        if (request == null || request.getNombre() == null || request.getNombre().isBlank()) {
            throw new SchedulingException("El nombre del medico/terapista es obligatorio");
        }
        Doctor doctor = new Doctor(null, request.getNombre().trim(), request.isActivo());
        return doctorRepository.save(doctor);
    }

    public Doctor actualizarDoctor(String id, DoctorRequest request) {
        if (id == null || id.isBlank()) {
            throw new SchedulingException("El ID del medico/terapista es obligatorio");
        }
        if (request == null || request.getNombre() == null || request.getNombre().isBlank()) {
            throw new SchedulingException("El nombre del medico/terapista es obligatorio");
        }
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new SchedulingException("El medico/terapista no existe"));

        doctor.setNombre(request.getNombre().trim());
        doctor.setActivo(request.isActivo());

        return doctorRepository.save(doctor);
    }
}