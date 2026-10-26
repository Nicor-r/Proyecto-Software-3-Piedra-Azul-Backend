package com.piedrazul.api.scheduling.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @file SpringDataDoctorRepository.java
 * @brief Interfaz Spring Data JPA para {@link DoctorJpaEntity}.
 */
@Repository
public interface SpringDataDoctorRepository extends JpaRepository<DoctorJpaEntity, String> {
}
