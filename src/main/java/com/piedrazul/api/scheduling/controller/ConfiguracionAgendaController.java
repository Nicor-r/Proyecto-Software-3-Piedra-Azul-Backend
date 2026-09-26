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
 * @file ConfiguracionAgendaController.java
 * @brief Controlador REST de administración para la configuración de agenda (HE-03).
 *
 * @details
 * Expone las operaciones necesarias para crear, actualizar y consultar la
 * configuración de agenda de un doctor.
 *
 * Endpoints principales:
 * - {@code PUT /api/admin/schedule-config}: crea o actualiza la configuración
 *   de un doctor. La operación es idempotente.
 * - {@code GET /api/admin/schedule-config/{doctorId}}: consulta la configuración
 *   actual de un doctor. Se usa para precargar el formulario de edición en el
 *   frontend. Retorna 404 si el doctor aún no tiene configuración guardada.
 *
 * TODO:
 * Si el frontend prefiere 4 botones GUARDAR independientes —uno por HU-3,
 * HU-3.1, HU-3.2 y HU-3.3— el PUT se puede dividir en 4 PATCH más pequeños
 * reutilizando el mismo Service.
 */
@RestController
@RequestMapping("/api/admin/schedule-config")
public class ConfiguracionAgendaController {

    /**
     * @brief Servicio encargado de la lógica de negocio para la configuración de agenda.
     */
    private final ConfiguracionAgendaService configuracionAgendaService;

    /**
     * @brief Mapper encargado de convertir entre entidades de dominio y DTOs de respuesta.
     */
    private final ConfiguracionAgendaMapper configuracionAgendaMapper;

    /**
     * @brief Construye el controlador con sus dependencias.
     *
     * @param configuracionAgendaService Servicio de configuración de agenda.
     * @param configuracionAgendaMapper  Mapper de configuración de agenda.
     */
    public ConfiguracionAgendaController(ConfiguracionAgendaService configuracionAgendaService,
                                          ConfiguracionAgendaMapper configuracionAgendaMapper) {
        this.configuracionAgendaService = configuracionAgendaService;
        this.configuracionAgendaMapper = configuracionAgendaMapper;
    }

    /**
     * @brief Crea o actualiza la configuración de agenda de un doctor.
     *
     * @details
     * La operación es idempotente: si ya existe una configuración para el doctor,
     * se actualiza; si no existe, se crea.
     *
     * @param request Datos de entrada con la configuración de agenda a guardar.
     * @return {@link ResponseEntity} con estado 200 y la configuración guardada
     *         en formato {@link ConfiguracionAgendaResponse}.
     */
    @PutMapping
    public ResponseEntity<ConfiguracionAgendaResponse> configurar(
            @RequestBody @Valid ConfiguracionAgendaRequest request) {
        ConfiguracionAgenda configuracion = configuracionAgendaService.configurarAgenda(request);
        return ResponseEntity.ok(configuracionAgendaMapper.toResponse(configuracion));
    }

    /**
     * @brief Consulta la configuración de agenda actual de un doctor.
     *
     * @details
     * Si el doctor no tiene configuración guardada, retorna 404 Not Found.
     *
     * @param doctorId Identificador del doctor.
     * @return {@link ResponseEntity} con estado 200 y la configuración encontrada,
     *         o estado 404 si no existe configuración para el doctor.
     */
    @GetMapping("/{doctorId}")
    public ResponseEntity<ConfiguracionAgendaResponse> obtener(@PathVariable String doctorId) {
        return configuracionAgendaService.consultarConfiguracion(doctorId)
                .map(configuracionAgendaMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * @brief Maneja las excepciones de tipo {@link SchedulingException}.
     *
     * @param ex Excepción de scheduling capturada.
     * @return {@link ResponseEntity} con estado 400 Bad Request y un cuerpo
     *         {@link ErrorResponse} con el mensaje de error.
     */
    @ExceptionHandler(SchedulingException.class)
    public ResponseEntity<ErrorResponse> handleSchedulingException(SchedulingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }
}