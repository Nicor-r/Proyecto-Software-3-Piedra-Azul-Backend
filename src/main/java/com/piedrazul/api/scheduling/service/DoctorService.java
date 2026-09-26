package com.piedrazul.api.scheduling.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

/**
 * Orquesta consultas de medicos/terapistas. Hoy es solo un "paso directo"
 * hacia DoctorRepository, pero mantenerlo como Service (en vez de que el
 * Controller llame al Repository directamente) deja el camino libre para
 * agregar reglas de negocio despues. Como filtrar por medicos activos, etc.
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
}
