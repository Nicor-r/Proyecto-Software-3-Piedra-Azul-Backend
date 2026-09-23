package com.piedrazul.api.scheduling.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SpringDataConfiguracionAgendaRepository
 */
public interface SpringDataConfiguracionAgendaRepository extends JpaRepository<ConfiguracionAgendaJpaEntity, String> {
    Optional<ConfiguracionAgendaJpaEntity> findByDoctorId(String doctorId);

    
}