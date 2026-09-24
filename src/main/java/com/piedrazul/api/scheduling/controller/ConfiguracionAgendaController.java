package com.piedrazul.api.scheduling.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.piedrazul.api.scheduling.domain.ConfiguracionAgenda;
import com.piedrazul.api.scheduling.dto.ConfiguracionAgendaRequest;
import com.piedrazul.api.scheduling.dto.ConfiguracionAgendaResponse;
import com.piedrazul.api.scheduling.exception.SchedulingException;
import com.piedrazul.api.scheduling.mapper.ConfiguracionAgendaMapper;
import com.piedrazul.api.scheduling.service.ConfiguracionAgendaService;

import jakarta.validation.Valid;

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

    @ExceptionHandler(SchedulingException.class)
    public ResponseEntity<String> handleSchedulingException(SchedulingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
