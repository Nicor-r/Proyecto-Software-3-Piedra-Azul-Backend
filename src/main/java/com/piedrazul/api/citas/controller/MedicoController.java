package com.piedrazul.api.citas.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.piedrazul.api.citas.dto.ArchivoCsv;
import com.piedrazul.api.citas.dto.HistorialPacienteResponse;
import com.piedrazul.api.citas.dto.MedicoPerfilResponse;
import com.piedrazul.api.citas.dto.PacienteSugerenciaDTO;
import com.piedrazul.api.citas.security.AuthenticatedUser;
import com.piedrazul.api.citas.service.ExportacionCitasMedicoService;
import com.piedrazul.api.citas.service.HistorialPacienteService;
import com.piedrazul.api.citas.service.MedicoActualService;
import com.piedrazul.api.citas.service.SugerenciaPacientesService;
import com.piedrazul.api.scheduling.domain.Doctor;

/**
 * Endpoints exclusivos del actor MEDICO (HE-04 y HE-05).
 * Todas las rutas /api/medico/** estan protegidas por MedicoAuthorizationInterceptor.
 */
@RestController
@RequestMapping("/api/medico")
public class MedicoController {

    private final MedicoActualService medicoActualService;
    private final HistorialPacienteService historialPacienteService;
    private final SugerenciaPacientesService sugerenciaPacientesService;
    private final ExportacionCitasMedicoService exportacionCitasMedicoService;

    public MedicoController(MedicoActualService medicoActualService,
                            HistorialPacienteService historialPacienteService,
                            SugerenciaPacientesService sugerenciaPacientesService,
                            ExportacionCitasMedicoService exportacionCitasMedicoService) {
        this.medicoActualService = medicoActualService;
        this.historialPacienteService = historialPacienteService;
        this.sugerenciaPacientesService = sugerenciaPacientesService;
        this.exportacionCitasMedicoService = exportacionCitasMedicoService;
    }

    @GetMapping("/perfil")
    public ResponseEntity<MedicoPerfilResponse> obtenerPerfil(@AuthenticatedUser String email) {
        Doctor medico = medicoActualService.obtenerMedicoActual(email);
        return ResponseEntity.ok(new MedicoPerfilResponse(medico.getId(), medico.getNombre()));
    }

    /**
     * HE-05 / HU-5: historial de citas de un paciente a partir de su cedula.
     * "cedula" es opcional en la peticion para que, si llega vacia o no llega,
     * el servicio responda el mensaje del escenario 4 en lugar de un error generico.
     */
    @GetMapping("/historial")
    public ResponseEntity<HistorialPacienteResponse> consultarHistorial(
            @RequestParam(required = false) String cedula) {
        return ResponseEntity.ok(historialPacienteService.consultarPorCedula(cedula));
    }

    /**
     * HE-05 / HU-5.1: autocompletado. Hasta 10 pacientes cuya cedula empieza por
     * los digitos escritos. Con menos de 3 digitos responde una lista vacia.
     */
    @GetMapping("/pacientes/sugerencias")
    public ResponseEntity<List<PacienteSugerenciaDTO>> sugerirPacientes(
            @RequestParam(required = false) String cedula) {
        return ResponseEntity.ok(sugerenciaPacientesService.sugerirPorCedula(cedula));
    }

    /**
     * HE-04 / HU-4 y HU-4.1: descarga en CSV las citas del medico autenticado
     * en una fecha, opcionalmente filtradas por estado (vacio o "TODOS" = todas).
     */
    @GetMapping("/citas/exportar")
    public ResponseEntity<byte[]> exportarCitas(
            @AuthenticatedUser String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String estado) {

        ArchivoCsv archivo = exportacionCitasMedicoService.exportarCitasDelMedico(email, fecha, estado);

        ContentDisposition descarga = ContentDisposition.attachment()
                .filename(archivo.nombreArchivo())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, descarga.toString())
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(archivo.contenido());
    }
}