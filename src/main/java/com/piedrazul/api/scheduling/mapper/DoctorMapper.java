package com.piedrazul.api.scheduling.mapper;

import org.springframework.stereotype.Component;

import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.dto.DoctorResponse;

@Component
public class DoctorMapper {

    public DoctorResponse toResponse(Doctor doctor) {
        return new DoctorResponse(doctor.getId(), doctor.getNombre(), doctor.isActivo());
    }
}
