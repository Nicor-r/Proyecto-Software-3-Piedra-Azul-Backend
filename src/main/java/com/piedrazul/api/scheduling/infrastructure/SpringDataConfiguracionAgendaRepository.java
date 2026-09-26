package com.piedrazul.api.scheduling.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @file SpringDataConfiguracionAgendaRepository.java
 * @brief Repositorio Spring Data JPA para {@link ConfiguracionAgendaJpaEntity}.
 *
 * @details
 * Proporciona las operaciones CRUD estándar heredadas de {@link JpaRepository}
 * y una consulta derivada por {@code doctorId}. Es consumido por el adaptador
 * de persistencia que implementa {@code ConfiguracionAgendaRepository}.
 */
public interface SpringDataConfiguracionAgendaRepository extends JpaRepository<ConfiguracionAgendaJpaEntity, String> {

    /**
     * @brief Busca la configuración de agenda asociada a un doctor.
     *
     * @param doctorId Identificador del doctor.
     * @return {@link Optional} con la configuración si existe, o vacío en caso contrario.
     */
    Optional<ConfiguracionAgendaJpaEntity> findByDoctorId(String doctorId);
}