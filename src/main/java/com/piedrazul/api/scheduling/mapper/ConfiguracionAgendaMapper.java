package com.piedrazul.api.scheduling.mapper;

import org.springframework.stereotype.Component;

import com.piedrazul.api.scheduling.domain.ConfiguracionAgenda;
import com.piedrazul.api.scheduling.dto.ConfiguracionAgendaResponse;

@Component
public class ConfiguracionAgendaMapper {

    public ConfiguracionAgendaResponse toResponse(ConfiguracionAgenda configuracion) {
        return new ConfiguracionAgendaResponse(
                configuracion.getId(),
                configuracion.getDoctorId(),
                configuracion.getVentanaAgendamiento().getSemanas(),
                configuracion.getDiasAtencion().getDias(),
                configuracion.getFranjaHoraria().getHoraInicio(),
                configuracion.getFranjaHoraria().getHoraFin(),
                configuracion.getIntervaloCitas().getMinutos());
    }
}
