package com.piedrazul.api.scheduling.mapper;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.piedrazul.api.scheduling.domain.ConfiguracionAgenda;
import com.piedrazul.api.scheduling.domain.DiasAtencion;
import com.piedrazul.api.scheduling.domain.FranjaHoraria;
import com.piedrazul.api.scheduling.domain.IntervaloCitas;
import com.piedrazul.api.scheduling.domain.VentanaAgendamiento;
import com.piedrazul.api.scheduling.infrastructure.ConfiguracionAgendaJpaEntity;


@Component
public class ConfiguracionAgendaEntityMapper {

    public ConfiguracionAgendaJpaEntity toEntity(ConfiguracionAgenda configuracion) {
        ConfiguracionAgendaJpaEntity entity = new ConfiguracionAgendaJpaEntity();
        entity.setId(configuracion.getId());
        entity.setDoctorId(configuracion.getDoctorId());
        entity.setVentanaSemanas(configuracion.getVentanaAgendamiento().getSemanas());
        entity.setDiasAtencion(serializarDias(configuracion.getDiasAtencion().getDias()));
        entity.setHoraInicio(configuracion.getFranjaHoraria().getHoraInicio().toString());
        entity.setHoraFin(configuracion.getFranjaHoraria().getHoraFin().toString());
        entity.setIntervaloMinutos(configuracion.getIntervaloCitas().getMinutos());
        return entity;
    }

    public ConfiguracionAgenda toDomain(ConfiguracionAgendaJpaEntity entity) {
        return new ConfiguracionAgenda(
                entity.getId(),
                entity.getDoctorId(),
                new VentanaAgendamiento(entity.getVentanaSemanas()),
                new DiasAtencion(deserializarDias(entity.getDiasAtencion())),
                new FranjaHoraria(LocalTime.parse(entity.getHoraInicio()), LocalTime.parse(entity.getHoraFin())),
                new IntervaloCitas(entity.getIntervaloMinutos())
        );
    }

    private String serializarDias(Set<DayOfWeek> dias) {
        return dias.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    private Set<DayOfWeek> deserializarDias(String valor) {
        return Arrays.stream(valor.split(","))
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DayOfWeek.class)));
    }
}
