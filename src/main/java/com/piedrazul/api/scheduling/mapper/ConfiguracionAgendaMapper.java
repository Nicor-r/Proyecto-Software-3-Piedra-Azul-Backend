package com.piedrazul.api.scheduling.mapper;

import org.springframework.stereotype.Component;

import com.piedrazul.api.scheduling.domain.ConfiguracionAgenda;
import com.piedrazul.api.scheduling.dto.ConfiguracionAgendaResponse;

/**
 * @file ConfiguracionAgendaMapper.java
 * @brief Mapper entre la entidad de dominio {@link ConfiguracionAgenda} y el
 *        DTO de salida {@link ConfiguracionAgendaResponse}.
 *
 * @details
 * Aplana los Value Objects del dominio para exponerlos al cliente. Su función
 * es análoga a la de {@code ConfiguracionAgendaEntityMapper}, pero orientada a
 * la capa HTTP en lugar de a la persistencia.
 */
@Component
public class ConfiguracionAgendaMapper {

    /**
     * @brief Convierte la configuración de dominio en su DTO de respuesta.
     *
     * @param configuracion Configuración de dominio.
     * @return El DTO de respuesta equivalente.
     */
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