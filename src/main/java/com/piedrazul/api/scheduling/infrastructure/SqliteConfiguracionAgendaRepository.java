package com.piedrazul.api.scheduling.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.piedrazul.api.scheduling.domain.ConfiguracionAgenda;
import com.piedrazul.api.scheduling.mapper.ConfiguracionAgendaEntityMapper;
import com.piedrazul.api.scheduling.repository.ConfiguracionAgendaRepository;

@Repository 
public class SqliteConfiguracionAgendaRepository implements ConfiguracionAgendaRepository{
    private final SpringDataConfiguracionAgendaRepository springDataRepository;
    private final ConfiguracionAgendaEntityMapper mapper;

    public SqliteConfiguracionAgendaRepository(SpringDataConfiguracionAgendaRepository springDataRepository,
                                                ConfiguracionAgendaEntityMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public ConfiguracionAgenda save(ConfiguracionAgenda configuracion) {
        if (configuracion.getId() == null) {
            configuracion.setId(UUID.randomUUID().toString());
        }
        ConfiguracionAgendaJpaEntity entity = mapper.toEntity(configuracion);
        ConfiguracionAgendaJpaEntity savedEntity = springDataRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ConfiguracionAgenda> findByDoctorId(String doctorId) {
        return springDataRepository.findByDoctorId(doctorId).map(mapper::toDomain);
    }
    
}
