package com.piedrazul.api.scheduling.mapper;

import org.springframework.stereotype.Component;

import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.infrastructure.DoctorJpaEntity;

/**
 * @file DoctorEntityMapper.java
 * @brief Mapper entre la entidad de dominio {@link Doctor} y la entidad JPA {@link DoctorJpaEntity}.
 */
@Component
public class DoctorEntityMapper {

    public Doctor toDomain(DoctorJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Doctor(entity.getId(), entity.getNombre(), entity.isActivo());
    }

    public DoctorJpaEntity toEntity(Doctor domain) {
        if (domain == null) {
            return null;
        }
        return new DoctorJpaEntity(domain.getId(), domain.getNombre(), domain.isActivo());
    }
}
