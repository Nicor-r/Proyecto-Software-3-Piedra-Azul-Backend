package com.piedrazul.api.scheduling.repository;

import java.util.Optional;
import com.piedrazul.api.scheduling.domain.ConfiguracionAgenda;

/**
 * ConfiguracionAgendaRepository
 */
public interface ConfiguracionAgendaRepository {

    ConfiguracionAgenda save(ConfiguracionAgenda configuracion);

    Optional<ConfiguracionAgenda> findByDoctorId(String doctorId);
}