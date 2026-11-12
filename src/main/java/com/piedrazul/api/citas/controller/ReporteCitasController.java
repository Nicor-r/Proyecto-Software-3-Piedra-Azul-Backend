package com.piedrazul.api.citas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.piedrazul.api.citas.dto.ReporteCitasResponse;
import com.piedrazul.api.citas.service.ReporteCitasService;

@RestController
@RequestMapping("/api/reportes")
public class ReporteCitasController {

    private final ReporteCitasService reporteCitasService;

    public ReporteCitasController(ReporteCitasService reporteCitasService) {
        this.reporteCitasService = reporteCitasService;
    }

    @GetMapping("/citas-por-mes")
    public ResponseEntity<ReporteCitasResponse> obtenerCitasPorMes(
            @RequestParam Integer anio,
            @RequestParam(required = false) String medicoId,
            @RequestParam(required = false, defaultValue = "POR_MES") String tipo) {
        return ResponseEntity.ok(reporteCitasService.generarReporteCitasPorMes(anio, medicoId, tipo));
    }
}
