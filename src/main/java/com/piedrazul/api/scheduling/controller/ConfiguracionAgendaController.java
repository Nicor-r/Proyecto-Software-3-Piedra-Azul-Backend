package com.piedrazul.api.scheduling.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.piedrazul.api.scheduling.domain.ConfiguracionAgenda;
import com.piedrazul.api.scheduling.dto.ConfiguracionAgendaRequest;
import com.piedrazul.api.scheduling.dto.ConfiguracionAgendaResponse;
import com.piedrazul.api.scheduling.dto.ErrorResponse;
import com.piedrazul.api.scheduling.exception.SchedulingException;
import com.piedrazul.api.scheduling.mapper.ConfiguracionAgendaMapper;
import com.piedrazul.api.scheduling.service.ConfiguracionAgendaService;

import jakarta.validation.Valid;

/**
 * Endpoint de administracion para HE-03.
 *  - PUT: crea o actualiza la configuracion de un doctor (idempotente).
 *  - GET /{doctorId}: consulta la configuracion actual, para precargar el
 *    formulario de edicion en el frontend. 404 si el doctor aun no tiene
 *    configuracion guardada.
 * (TODO: si el front prefiere 4 botones GUARDAR independientes
 * -uno por HU-3, HU-3.1, HU-3.2, HU-3.3- el PUT se puede dividir en 4 PATCH
 * mas pequenos reutilizando el mismo Service.)
 */
@RestController
@RequestMapping("/api/admin/schedule-config")
public class ConfiguracionAgendaController {

    private final ConfiguracionAgendaService configuracionAgendaService;
    private final ConfiguracionAgendaMapper configuracionAgendaMapper;

    public ConfiguracionAgendaController(ConfiguracionAgendaService configuracionAgendaService,
                                          ConfiguracionAgendaMapper configuracionAgendaMapper) {
        this.configuracionAgendaService = configuracionAgendaService;
        this.configuracionAgendaMapper = configuracionAgendaMapper;
    }

    @PutMapping
    public ResponseEntity<ConfiguracionAgendaResponse> configurar(
            @RequestBody @Valid ConfiguracionAgendaRequest request) {
        ConfiguracionAgenda configuracion = configuracionAgendaService.configurarAgenda(request);
        return ResponseEntity.ok(configuracionAgendaMapper.toResponse(configuracion));
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<ConfiguracionAgendaResponse> obtener(@PathVariable String doctorId) {
        return configuracionAgendaService.consultarConfiguracion(doctorId)
                .map(configuracionAgendaMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(SchedulingException.class)
    public ResponseEntity<ErrorResponse> handleSchedulingException(SchedulingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }
}
