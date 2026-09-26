package com.piedrazul.api.citas.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.piedrazul.api.citas.dto.AgendarCitaRequest;
import com.piedrazul.api.citas.dto.CitaDTO;
import com.piedrazul.api.citas.dto.FranjaDTO;
import com.piedrazul.api.citas.security.AuthenticatedUser;
import com.piedrazul.api.citas.service.CitaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<FranjaDTO>> getFranjasDisponibles(
            @RequestParam String medicoId,
            @RequestParam LocalDate fecha) {
        return ResponseEntity.ok(citaService.obtenerFranjasDisponibles(medicoId, fecha));
    }

    /**
     * Agenda una cita para el paciente autenticado. El email del usuario se
     * obtiene del JWT mediante AuthenticatedUserArgumentResolver.
     */
    @PostMapping("/agendar")
    public ResponseEntity<CitaDTO> agendarCita(@RequestBody @Valid AgendarCitaRequest request,
            @AuthenticatedUser String pacienteEmail) {
        CitaDTO cita = citaService.agendarCita(request, pacienteEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(cita);
    }

    @GetMapping("/mis-citas")
    public ResponseEntity<List<CitaDTO>> obtenerMisCitas(@AuthenticatedUser String pacienteEmail) {
        return ResponseEntity.ok(citaService.obtenerMisCitas(pacienteEmail));
    }
}
