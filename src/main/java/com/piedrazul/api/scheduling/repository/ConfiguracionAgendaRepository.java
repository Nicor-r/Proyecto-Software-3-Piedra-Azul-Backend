package com.piedrazul.api.scheduling.repository;

import java.util.Optional;
import com.piedrazul.api.scheduling.domain.ConfiguracionAgenda;

/**
 * @file ConfiguracionAgendaRepository.java
 * @brief Puerto de persistencia para {@link ConfiguracionAgenda}.
 *
 * @details
 * Define el contrato que debe cumplir cualquier implementación de persistencia
 * de configuraciones de agenda. Al vivir en el dominio, desacopla al servicio
 * de la tecnología concreta de almacenamiento (por ejemplo, la implementación
 * SQLite en {@code infrastructure}).
 */
public interface ConfiguracionAgendaRepository {

    /**
     * @brief Persiste la configuración de agenda.
     *
     * @details
     * Crea un nuevo registro si la configuración no tiene id, o actualiza el
     * existente en caso contrario. La implementación es responsable de asignar
     * el id cuando falte.
     *
     * @param configuracion Configuración a guardar.
     * @return La configuración persistida, con su id asignado.
     */
    ConfiguracionAgenda save(ConfiguracionAgenda configuracion);

    /**
     * @brief Busca la configuración de agenda asociada a un doctor.
     *
     * @param doctorId Identificador del doctor.
     * @return {@link Optional} con la configuración si existe, o vacío en caso contrario.
     */
    Optional<ConfiguracionAgenda> findByDoctorId(String doctorId);
}