package com.piedrazul.api.scheduling.mapper;

import org.springframework.stereotype.Component;

import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.dto.DoctorResponse;

/**
 * @file DoctorMapper.java
 * @brief Mapper entre la entidad de dominio {@link Doctor} y el DTO de salida
 *        {@link DoctorResponse}.
 *
 * @details
 * Convierte el doctor de dominio al objeto expuesto al cliente, evitando
 * exponer la entidad directamente en el JSON de respuesta.
 */
@Component
public class DoctorMapper {

    /**
     * @brief Convierte un doctor de dominio en su DTO de respuesta.
     *
     * @param doctor Doctor de dominio.
     * @return El DTO de respuesta equivalente.
     */
    public DoctorResponse toResponse(Doctor doctor) {
        return new DoctorResponse(doctor.getId(), doctor.getNombre(), doctor.isActivo());
    }
}