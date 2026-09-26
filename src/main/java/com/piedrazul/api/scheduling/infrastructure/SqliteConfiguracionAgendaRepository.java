package com.piedrazul.api.scheduling.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.piedrazul.api.scheduling.domain.ConfiguracionAgenda;
import com.piedrazul.api.scheduling.mapper.ConfiguracionAgendaEntityMapper;
import com.piedrazul.api.scheduling.repository.ConfiguracionAgendaRepository;

/**
 * @file SqliteConfiguracionAgendaRepository.java
 * @brief Adaptador de persistencia que implementa
 *        {@link ConfiguracionAgendaRepository} sobre SQLite vía Spring Data JPA.
 *
 * @details
 * Actúa como puente entre el dominio y la capa de persistencia: delega el
 * acceso a datos en {@link SpringDataConfiguracionAgendaRepository} y usa
 * {@link ConfiguracionAgendaEntityMapper} para convertir entre
 * {@link ConfiguracionAgenda} y {@link ConfiguracionAgendaJpaEntity}.
 *
 * El identificador se genera aquí (UUID) cuando la configuración aún no lo
 * tiene, de modo que la capa de dominio no necesita conocer cómo se asignan
 * los ids.
 */
@Repository
public class SqliteConfiguracionAgendaRepository implements ConfiguracionAgendaRepository {

    private final SpringDataConfiguracionAgendaRepository springDataRepository;
    private final ConfiguracionAgendaEntityMapper mapper;

    /**
     * @brief Construye el adaptador con sus dependencias.
     *
     * @param springDataRepository Repositorio Spring Data subyacente.
     * @param mapper               Mapper entre dominio y entidad JPA.
     */
    public SqliteConfiguracionAgendaRepository(SpringDataConfiguracionAgendaRepository springDataRepository,
                                                ConfiguracionAgendaEntityMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    /**
     * @brief Persiste la configuración de agenda.
     *
     * @details
     * Si la configuración no tiene {@code id}, se le asigna un UUID antes de
     * guardarla. La operación es idempotente respecto al contenido: guardar la
     * misma configuración con el mismo id actualiza el registro existente.
     *
     * @param configuracion Configuración a guardar.
     * @return La configuración persistida, ya con su id asignado.
     */
    @Override
    public ConfiguracionAgenda save(ConfiguracionAgenda configuracion) {
        if (configuracion.getId() == null) {
            configuracion.setId(UUID.randomUUID().toString());
        }
        ConfiguracionAgendaJpaEntity entity = mapper.toEntity(configuracion);
        ConfiguracionAgendaJpaEntity savedEntity = springDataRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    /**
     * @brief Busca la configuración de agenda asociada a un doctor.
     *
     * @param doctorId Identificador del doctor.
     * @return {@link Optional} con la configuración si existe, o vacío en caso contrario.
     */
    @Override
    public Optional<ConfiguracionAgenda> findByDoctorId(String doctorId) {
        return springDataRepository.findByDoctorId(doctorId).map(mapper::toDomain);
    }

}