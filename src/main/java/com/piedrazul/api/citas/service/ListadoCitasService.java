package com.piedrazul.api.citas.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.piedrazul.api.citas.domain.Cita;
import com.piedrazul.api.citas.domain.EstadoCita;
import com.piedrazul.api.citas.dto.CitaDTO;
import com.piedrazul.api.citas.dto.ListadoCitasResponse;
import com.piedrazul.api.citas.infrastructure.repository.CitaRepository;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.infrastructure.entity.TerapistaJpaEntity;
import com.piedrazul.api.iam.infrastructure.repository.SpringDataTerapistaRepository;
import com.piedrazul.api.iam.repository.UserRepository;
import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

@Service
public class ListadoCitasService {

    private static final String ESTADO_TODOS = "TODOS";

    private final CitaRepository citaRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SpringDataTerapistaRepository terapistaRepository; // <-- NUEVO

    public ListadoCitasService(CitaRepository citaRepository,
            DoctorRepository doctorRepository,
            UserRepository userRepository,
            SpringDataTerapistaRepository terapistaRepository) { // <-- NUEVO
        this.citaRepository = citaRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.terapistaRepository = terapistaRepository;
    }

    public ListadoCitasResponse listarCitas(String medicoId, LocalDate fecha, String estadoTexto) {
        boolean tieneMedico = medicoId != null && !medicoId.isBlank();
        boolean tieneFecha = fecha != null;

        if (tieneFecha) {
            validarFechaNoAnterior(fecha);
        }

        // Quitamos la validación estricta de Doctor para permitir terapistas
        if (tieneMedico) {
            validarMedicoDisponible(medicoId);
        }

        EstadoCita estado = parseEstadoOrNull(estadoTexto);

        List<Cita> citas;
        if (tieneMedico && tieneFecha) {
            citas = (estado == null)
                    ? citaRepository.findByMedicoIdAndFechaOrderByHoraAsc(medicoId, fecha)
                    : citaRepository.findByMedicoIdAndFechaAndEstadoOrderByHoraAsc(medicoId, fecha, estado);
        } else if (tieneMedico) {
            citas = (estado == null)
                    ? citaRepository.findByMedicoIdOrderByFechaAscHoraAsc(medicoId)
                    : citaRepository.findByMedicoIdAndEstadoOrderByFechaAscHoraAsc(medicoId, estado);
        } else if (tieneFecha) {
            citas = (estado == null)
                    ? citaRepository.findByFechaOrderByHoraAsc(fecha)
                    : citaRepository.findByFechaAndEstadoOrderByHoraAsc(fecha, estado);
        } else {
            citas = (estado == null)
                    ? citaRepository.findAll()
                    : citaRepository.findAll().stream().filter(c -> c.getEstado() == estado).toList();
        }

        List<CitaDTO> citasDTO = citas.stream().map(this::toDTO).toList();
        return new ListadoCitasResponse(citasDTO.size(), citasDTO);
    }

    private void validarFechaNoAnterior(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha ingresada no es valida");
        }
    }

    private void validarMedicoDisponible(String medicoId) {
        Doctor doctor = doctorRepository.findById(medicoId).orElse(null);
        if (doctor != null && doctor.isActivo()) {
            return; // Es un doctor válido
        }
        // Si no es doctor, verificamos si es un terapista
        if (terapistaRepository.findById(medicoId).isPresent()) {
            return; // Es un terapista válido
        }
        throw new IllegalArgumentException("El medico/terapista seleccionado no esta disponible");
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
        // Buscar en tabla doctores o terapistas
        String medicoNombre = doctorRepository.findById(cita.getMedicoId())
                .map(Doctor::getNombre)
                .orElseGet(() -> terapistaRepository.findById(cita.getMedicoId())
                        .map(TerapistaJpaEntity::getNombre)
                        .orElse(cita.getMedicoId()));

        // Buscar paciente por ID o por Email
        String pacienteNombre = userRepository.findById(cita.getPacienteId())
                .map(User::getNombreCompleto)
                .orElseGet(() -> userRepository.findByEmail(cita.getPacienteId())
                        .map(User::getNombreCompleto)
                        .orElse(cita.getPacienteId()));

        return new CitaDTO(
                cita.getId(),
                cita.getPacienteId(),
                cita.getMedicoId(),
                cita.getFecha(),
                cita.getHora(),
                cita.getEstado(),
                pacienteNombre,
                medicoNombre
        );
    }
}
