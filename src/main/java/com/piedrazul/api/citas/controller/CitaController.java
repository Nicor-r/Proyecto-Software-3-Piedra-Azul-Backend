package com.piedrazul.api.citas.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.piedrazul.api.citas.domain.Cita;
import com.piedrazul.api.citas.dto.AgendarCitaRequest;
import com.piedrazul.api.citas.dto.CitaDTO;
import com.piedrazul.api.citas.dto.CitaDetalleDTO;
import com.piedrazul.api.citas.dto.FranjaDTO;
import com.piedrazul.api.citas.security.AuthenticatedUser;
import com.piedrazul.api.citas.service.CitaCsvExporter;
import com.piedrazul.api.citas.service.CitaService;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.infrastructure.entity.TerapistaJpaEntity;
import com.piedrazul.api.iam.infrastructure.repository.SpringDataTerapistaRepository;
import com.piedrazul.api.iam.repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;
    private final CitaCsvExporter csvExporter;
    private final UserRepository userRepository;
    private final SpringDataTerapistaRepository terapistaRepository; // NUEVO

    public CitaController(CitaService citaService, 
                          CitaCsvExporter csvExporter,
                          UserRepository userRepository,
                          SpringDataTerapistaRepository terapistaRepository) {
        this.citaService = citaService;
        this.csvExporter = csvExporter;
        this.userRepository = userRepository;
        this.terapistaRepository = terapistaRepository;
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<FranjaDTO>> getFranjasDisponibles(
            @RequestParam String medicoId,
            @RequestParam LocalDate fecha) {
        return ResponseEntity.ok(citaService.obtenerFranjasDisponibles(medicoId, fecha));
    }

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

    /**
     * Exporta el CSV de citas para una fecha específica.
     * El ID del terapista se deduce automáticamente del token JWT.
     */
    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportarCitas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @AuthenticatedUser String email) {

        // 1. Buscar el usuario por el email del token
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        // 2. Buscar el terapista vinculado a este usuario
        TerapistaJpaEntity terapista = terapistaRepository.findByUserId(user.getId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.FORBIDDEN, "El usuario no tiene un perfil de terapista asignado"));

        // 3. Usar el ID del terapista para buscar las citas y generar el CSV
        List<CitaDetalleDTO> citas = citaService.listarDetallePorFechaYTerapista(fecha, terapista.getId());
        byte[] csv = csvExporter.exportar(citas);

        String filename = "citas_" + fecha + "_" + terapista.getId() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/paciente/{cedula}")
    public ResponseEntity<List<Cita>> historialPorCedula(@PathVariable String cedula) {
        return ResponseEntity.ok(citaService.listarPorCedulaPaciente(cedula));
    }
}