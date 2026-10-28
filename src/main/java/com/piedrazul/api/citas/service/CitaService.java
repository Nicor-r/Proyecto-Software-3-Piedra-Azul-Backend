package com.piedrazul.api.citas.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
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
import com.piedrazul.api.scheduling.domain.ConfiguracionAgenda;
import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.ConfiguracionAgendaRepository;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

@Service
public class CitaService {

    private static final Logger log = LoggerFactory.getLogger(CitaService.class);

    private static final LocalTime DEFAULT_HORA_INICIO = LocalTime.of(8, 0);
    private static final LocalTime DEFAULT_HORA_FIN = LocalTime.of(17, 0);
    private static final int DEFAULT_INTERVALO_MINUTOS = 30;
    private static final int DEFAULT_VENTANA_SEMANAS = 4;
    private static final Set<DayOfWeek> DEFAULT_DIAS_ATENCION = EnumSet.allOf(DayOfWeek.class);

    private final CitaRepository citaRepository;
    private final ConfiguracionAgendaRepository configuracionAgendaRepository;
    private final DoctorRepository doctorRepository;

    public CitaService(CitaRepository citaRepository,
                       ConfiguracionAgendaRepository configuracionAgendaRepository,
                       DoctorRepository doctorRepository) {
        this.citaRepository = citaRepository;
        this.configuracionAgendaRepository = configuracionAgendaRepository;
        this.doctorRepository = doctorRepository;
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

        validarMedicoDisponible(medicoId);

        Optional<ConfiguracionAgenda> configOpt = configuracionAgendaRepository.findByDoctorId(medicoId);

        int ventanaSemanas = configOpt.map(c -> c.getVentanaAgendamiento().getSemanas()).orElse(DEFAULT_VENTANA_SEMANAS);
        if (fecha.isAfter(LocalDate.now().plusWeeks(ventanaSemanas))) {
            throw new IllegalArgumentException("La fecha excede la ventana de agendamiento permitida (" + ventanaSemanas + " semanas)");
        }

        Set<DayOfWeek> diasPermitidos = configOpt.map(c -> c.getDiasAtencion().getDias()).orElse(DEFAULT_DIAS_ATENCION);
        if (!diasPermitidos.contains(fecha.getDayOfWeek())) {
            return Collections.emptyList();
        }

        LocalTime horaInicio = configOpt.map(c -> c.getFranjaHoraria().getHoraInicio()).orElse(DEFAULT_HORA_INICIO);
        LocalTime horaFin = configOpt.map(c -> c.getFranjaHoraria().getHoraFin()).orElse(DEFAULT_HORA_FIN);
        int intervaloMinutos = configOpt.map(c -> c.getIntervaloCitas().getMinutos()).orElse(DEFAULT_INTERVALO_MINUTOS);

        Set<LocalTime> horasOcupadas = citaRepository
                .findByMedicoIdAndFecha(medicoId, fecha)
                .stream()
                .map(Cita::getHora)
                .collect(Collectors.toSet());

        List<FranjaDTO> franjas = new ArrayList<>();
        LocalTime hora = horaInicio;
        while (!hora.plusMinutes(intervaloMinutos).isAfter(horaFin)) {
            boolean ocupada = horasOcupadas.contains(hora);
            boolean pasadaHoy = fecha.isEqual(LocalDate.now()) && hora.isBefore(LocalTime.now());
            boolean disponible = !ocupada && !pasadaHoy;

            franjas.add(new FranjaDTO(hora, disponible));
            hora = hora.plusMinutes(intervaloMinutos);
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

    private void validarMedicoDisponible(String medicoId) {
        Doctor doctor = doctorRepository.findById(medicoId)
                .orElseThrow(() -> new IllegalArgumentException("El medico/terapista seleccionado no esta disponible"));
        if (!doctor.isActivo()) {
            throw new IllegalArgumentException("El medico/terapista seleccionado no esta disponible");
        }
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

        validarMedicoDisponible(request.getMedicoId());

        Optional<ConfiguracionAgenda> configOpt = configuracionAgendaRepository.findByDoctorId(request.getMedicoId());

        int ventanaSemanas = configOpt.map(c -> c.getVentanaAgendamiento().getSemanas()).orElse(DEFAULT_VENTANA_SEMANAS);
        if (request.getFecha().isAfter(LocalDate.now().plusWeeks(ventanaSemanas))) {
            throw new IllegalArgumentException("La fecha excede la ventana de agendamiento permitida (" + ventanaSemanas + " semanas)");
        }

        Set<DayOfWeek> diasPermitidos = configOpt.map(c -> c.getDiasAtencion().getDias()).orElse(DEFAULT_DIAS_ATENCION);
        if (!diasPermitidos.contains(request.getFecha().getDayOfWeek())) {
            throw new IllegalArgumentException("El medico no atiende en el dia seleccionado");
        }

        LocalTime horaInicio = configOpt.map(c -> c.getFranjaHoraria().getHoraInicio()).orElse(DEFAULT_HORA_INICIO);
        LocalTime horaFin = configOpt.map(c -> c.getFranjaHoraria().getHoraFin()).orElse(DEFAULT_HORA_FIN);
        int intervaloMinutos = configOpt.map(c -> c.getIntervaloCitas().getMinutos()).orElse(DEFAULT_INTERVALO_MINUTOS);

        if (request.getHora().isBefore(horaInicio) || request.getHora().plusMinutes(intervaloMinutos).isAfter(horaFin)) {
            throw new IllegalArgumentException("La hora debe estar entre " + horaInicio + " y " + horaFin);
        }

        long segundosDesdeInicio = (long) request.getHora().toSecondOfDay() - horaInicio.toSecondOfDay();
        long intervaloSegundos = (long) intervaloMinutos * 60;
        if (segundosDesdeInicio % intervaloSegundos != 0) {
            throw new IllegalArgumentException("La hora seleccionada no corresponde a un intervalo valido");
        }

        if (request.getFecha().isEqual(LocalDate.now()) && request.getHora().isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("No se pueden agendar citas en horas pasadas");
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
