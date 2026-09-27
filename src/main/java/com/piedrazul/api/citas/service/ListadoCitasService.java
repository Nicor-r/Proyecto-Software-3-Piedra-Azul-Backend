package com.piedrazul.api.citas.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.piedrazul.api.citas.domain.Cita;
import com.piedrazul.api.citas.domain.EstadoCita;
import com.piedrazul.api.citas.dto.CitaDTO;
import com.piedrazul.api.citas.dto.ListadoCitasResponse;
import com.piedrazul.api.citas.infrastructure.repository.CitaRepository;
import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

/**
 * Orquesta RF1 (HE-01): listar las citas de un medico/terapista en una
 * fecha determinada (HU-1), con filtro opcional por estado (HU-1.1).
 *
 * Depende de CitaRepository (modulo citas, para los datos de las citas) y
 * de DoctorRepository (modulo scheduling, SOLO para validar que el medico
 * exista y este activo) - dos modulos distintos colaborando unicamente a
 * traves de interfaces, sin conocer los detalles internos del otro.
 *
 * IllegalArgumentException se usa a proposito para los errores de
 * validacion: el GlobalExceptionHandler que ya existe en este modulo la
 * atrapa automaticamente y responde 400 con el mensaje, sin que este
 * Service (ni ningun controller) tenga que preocuparse por el formato de
 * la respuesta de error.
 */
@Service
public class ListadoCitasService {

    private static final String ESTADO_TODOS = "TODOS";

    private final CitaRepository citaRepository;
    private final DoctorRepository doctorRepository;

    public ListadoCitasService(CitaRepository citaRepository, DoctorRepository doctorRepository) {
        this.citaRepository = citaRepository;
        this.doctorRepository = doctorRepository;
    }

    public ListadoCitasResponse listarCitas(String medicoId, LocalDate fecha, String estadoTexto) {
        validarFiltrosObligatorios(medicoId, fecha);
        validarFechaNoAnterior(fecha);
        validarMedicoDisponible(medicoId);
        EstadoCita estado = parseEstadoOrNull(estadoTexto);

        List<Cita> citas = (estado == null)
                ? citaRepository.findByMedicoIdAndFechaOrderByHoraAsc(medicoId, fecha)
                : citaRepository.findByMedicoIdAndFechaAndEstadoOrderByHoraAsc(medicoId, fecha, estado);

        List<CitaDTO> citasDTO = citas.stream().map(this::toDTO).toList();
        return new ListadoCitasResponse(citasDTO.size(), citasDTO);
    }

    private void validarFiltrosObligatorios(String medicoId, LocalDate fecha) {
        if (medicoId == null || medicoId.isBlank() || fecha == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un medico/terapista y una fecha para realizar la busqueda");
        }
    }

    private void validarFechaNoAnterior(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha ingresada no es valida");
        }
    }

    private void validarMedicoDisponible(String medicoId) {
        Doctor doctor = doctorRepository.findById(medicoId).orElse(null);
        if (doctor == null || !doctor.isActivo()) {
            throw new IllegalArgumentException("El medico/terapista seleccionado no esta disponible");
        }
    }

    private EstadoCita parseEstadoOrNull(String estadoTexto) {
        if (estadoTexto == null || estadoTexto.isBlank() || estadoTexto.equalsIgnoreCase(ESTADO_TODOS)) {
            return null;
        }
        try {
            return EstadoCita.valueOf(estadoTexto.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de cita invalido: " + estadoTexto);
        }
    }

    private CitaDTO toDTO(Cita cita) {
        return new CitaDTO(
                cita.getId(),
                cita.getPacienteId(),
                cita.getMedicoId(),
                cita.getFecha(),
                cita.getHora(),
                cita.getEstado()
        );
    }
}
