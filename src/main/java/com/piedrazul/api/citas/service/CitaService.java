package com.piedrazul.api.citas.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.piedrazul.api.citas.domain.Cita;
import com.piedrazul.api.citas.domain.EstadoCita;
import com.piedrazul.api.citas.dto.AgendarCitaRequest;
import com.piedrazul.api.citas.dto.CitaDTO;
import com.piedrazul.api.citas.dto.FranjaDTO;
import com.piedrazul.api.citas.exception.FranjaNoDisponibleException;
import com.piedrazul.api.citas.infrastructure.repository.CitaRepository;

@Service
public class CitaService {

    private static final Logger log = LoggerFactory.getLogger(CitaService.class);

    private static final LocalTime HORA_INICIO = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN = LocalTime.of(17, 0);
    private static final int DURACION_FRANJA_MINUTOS = 30;

    private final CitaRepository citaRepository;

    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Transactional(readOnly = true)
    public List<FranjaDTO> obtenerFranjasDisponibles(String medicoId, LocalDate fecha) {
        if (medicoId == null || medicoId.isBlank()) {
            throw new IllegalArgumentException("El medicoId es obligatorio");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden consultar fechas pasadas");
        }

        Set<LocalTime> horasOcupadas = citaRepository
                .findByMedicoIdAndFecha(medicoId, fecha)
                .stream()
                .map(Cita::getHora)
                .collect(Collectors.toSet());

        List<FranjaDTO> franjas = new ArrayList<>();
        LocalTime hora = HORA_INICIO;
        while (hora.isBefore(HORA_FIN)) {
            franjas.add(new FranjaDTO(hora, !horasOcupadas.contains(hora)));
            hora = hora.plusMinutes(DURACION_FRANJA_MINUTOS);
        }
        return franjas;
    }

    @Transactional
    public CitaDTO agendarCita(AgendarCitaRequest request, String pacienteId) {
        validarRequest(request, pacienteId);

        boolean ocupada = citaRepository.existsByMedicoIdAndFechaAndHora(
                request.getMedicoId(), request.getFecha(), request.getHora());

        if (ocupada) {
            throw new FranjaNoDisponibleException(
                    "La franja " + request.getHora() + " del " + request.getFecha() + " ya está ocupada");
        }

        Cita nuevaCita = new Cita(
                pacienteId,
                request.getMedicoId(),
                request.getFecha(),
                request.getHora(),
                EstadoCita.PENDIENTE
        );

        try {
            Cita guardada = citaRepository.save(nuevaCita);
            log.info("Cita agendada id={} paciente={} medico={} fecha={} hora={}",
                    guardada.getId(), pacienteId, request.getMedicoId(),
                    request.getFecha(), request.getHora());

            return toDTO(guardada);
        } catch (DataIntegrityViolationException e) {
            log.warn("Doble reserva detectada medico={} fecha={} hora={}",
                    request.getMedicoId(), request.getFecha(), request.getHora());
            throw new FranjaNoDisponibleException("La franja ya fue reservada por otro usuario");
        }
    }

    @Transactional(readOnly = true)
    public List<CitaDTO> obtenerMisCitas(String pacienteId) {
        if (pacienteId == null || pacienteId.isBlank()) {
            throw new IllegalArgumentException("El paciente no está autenticado");
        }

        return citaRepository.findByPacienteId(pacienteId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private void validarRequest(AgendarCitaRequest request, String pacienteId) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud no puede ser nula");
        }
        if (pacienteId == null || pacienteId.isBlank()) {
            throw new IllegalArgumentException("El paciente no está autenticado");
        }
        if (request.getMedicoId() == null || request.getMedicoId().isBlank()) {
            throw new IllegalArgumentException("El medicoId es obligatorio");
        }
        if (request.getFecha() == null || request.getHora() == null) {
            throw new IllegalArgumentException("La fecha y la hora son obligatorias");
        }
        if (request.getFecha().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden agendar citas en fechas pasadas");
        }
        if (request.getHora().isBefore(HORA_INICIO) || !request.getHora().isBefore(HORA_FIN)) {
            throw new IllegalArgumentException(
                    "La hora debe estar entre " + HORA_INICIO + " y " + HORA_FIN);
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
