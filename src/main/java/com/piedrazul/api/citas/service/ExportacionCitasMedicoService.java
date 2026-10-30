package com.piedrazul.api.citas.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.piedrazul.api.citas.domain.Cita;
import com.piedrazul.api.citas.domain.EstadoCita;
import com.piedrazul.api.citas.dto.ArchivoCsv;
import com.piedrazul.api.citas.dto.FilaExportacionCita;
import com.piedrazul.api.citas.infrastructure.repository.CitaRepository;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.repository.UserRepository;
import com.piedrazul.api.scheduling.domain.Doctor;

/**
 * Exporta a CSV las citas del MEDICO autenticado en una fecha (HE-04: HU-4 y HU-4.1).
 *
 * El medico NO se selecciona: es quien inicio sesion (decision del equipo).
 * El estado es opcional: vacio o "TODOS" exporta todas las citas.
 */
@Service
public class ExportacionCitasMedicoService {

    public static final String MENSAJE_FILTRO_INCOMPLETO =
            "Debe seleccionar un médico/terapista y una fecha para exportar";
    public static final String MENSAJE_SIN_CITAS =
            "No hay citas para exportar en la fecha y médico/terapista seleccionados";
    public static final String MENSAJE_ESTADO_SIN_CITAS =
            "No hay citas con el estado seleccionado para exportar";

    private static final String ESTADO_TODOS = "TODOS";

    private final MedicoActualService medicoActualService;
    private final CitaRepository citaRepository;
    private final UserRepository userRepository;
    private final ExportadorCsvCitas exportador;

    public ExportacionCitasMedicoService(MedicoActualService medicoActualService,
                                         CitaRepository citaRepository,
                                         UserRepository userRepository,
                                         ExportadorCsvCitas exportador) {
        this.medicoActualService = medicoActualService;
        this.citaRepository = citaRepository;
        this.userRepository = userRepository;
        this.exportador = exportador;
    }

    @Transactional(readOnly = true)
    public ArchivoCsv exportarCitasDelMedico(String emailMedico, LocalDate fecha, String estadoTexto) {
        // 1. HU-4 escenario 3: filtro incompleto (el medico ya viene del token; falta la fecha).
        if (fecha == null) {
            throw new IllegalArgumentException(MENSAJE_FILTRO_INCOMPLETO);
        }

        // 2. HU-4.1: estado opcional (null = todos).
        EstadoCita estado = convertirEstado(estadoTexto);

        // 3. HU-4 escenario 4: ¿que medico soy? (lanza "no disponible" si esta inactivo).
        Doctor medico = medicoActualService.obtenerMedicoActual(emailMedico);

        // 4. Citas del medico en la fecha, ordenadas por hora (con o sin filtro de estado).
        List<Cita> citas = (estado == null)
                ? citaRepository.findByMedicoIdAndFechaOrderByHoraAsc(medico.getId(), fecha)
                : citaRepository.findByMedicoIdAndFechaAndEstadoOrderByHoraAsc(medico.getId(), fecha, estado);

        // 5. HU-4 escenario 2 y HU-4.1 escenario 3: sin citas NO se genera archivo.
        if (citas.isEmpty()) {
            throw new IllegalArgumentException(estado == null ? MENSAJE_SIN_CITAS : MENSAJE_ESTADO_SIN_CITAS);
        }

        // 6. Un renglon por cita con los datos del paciente.
        List<FilaExportacionCita> filas = citas.stream()
                .map(this::convertirEnFila)
                .toList();

        // 7. La "imprenta" genera el archivo y su nombre.
        return new ArchivoCsv(
                exportador.nombreArchivo(medico.getNombre(), fecha),
                exportador.generar(filas));
    }

    private EstadoCita convertirEstado(String estadoTexto) {
        if (estadoTexto == null || estadoTexto.isBlank() || estadoTexto.trim().equalsIgnoreCase(ESTADO_TODOS)) {
            return null;
        }
        try {
            return EstadoCita.valueOf(estadoTexto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de cita inválido: " + estadoTexto);
        }
    }

    private FilaExportacionCita convertirEnFila(Cita cita) {
        // Decision del equipo para este corte: Cita.pacienteId guarda el CORREO del paciente.
        Optional<User> paciente = userRepository.findByEmail(cita.getPacienteId());

        return new FilaExportacionCita(
                cita.getHora(),
                paciente.map(User::getNombreCompleto).orElse(cita.getPacienteId()),
                paciente.map(User::getNumeroIdentificacion).orElse(""),
                paciente.map(User::getNumeroTelefonico).orElse(""),
                cita.getEstado());
    }
}