package com.piedrazul.api.citas.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.piedrazul.api.citas.dto.ListadoCitasResponse;
import com.piedrazul.api.citas.service.ListadoCitasService;

/**
 * Endpoint para el rol Agendador.
 * "estado" es opcional: ausente o "TODOS" trae todas las citas (HU-1);
 * un valor especifico (PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA)
 * filtra el listado (HU-1.1).
 */
@RestController
@RequestMapping("/api/citas")
public class AgendadorController {

    private final ListadoCitasService listadoCitasService;

    public AgendadorController(ListadoCitasService listadoCitasService) {
        this.listadoCitasService = listadoCitasService;
    }

    @GetMapping
    public ResponseEntity<ListadoCitasResponse> listarCitas(
            @RequestParam(required = false) String medicoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(listadoCitasService.listarCitas(medicoId, fecha, estado));
    }
}
