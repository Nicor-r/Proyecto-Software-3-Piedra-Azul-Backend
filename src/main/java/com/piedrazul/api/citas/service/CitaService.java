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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.piedrazul.api.citas.domain.Cita;
import com.piedrazul.api.citas.domain.EstadoCita;
import com.piedrazul.api.citas.dto.AgendarCitaRequest;
import com.piedrazul.api.citas.dto.CitaDTO;
import com.piedrazul.api.citas.dto.CitaDetalleDTO;
import com.piedrazul.api.citas.dto.FranjaDTO;
import com.piedrazul.api.citas.exception.FranjaNoDisponibleException;
import com.piedrazul.api.citas.infrastructure.repository.CitaRepository;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.infrastructure.repository.SpringDataTerapistaRepository;
import com.piedrazul.api.iam.repository.UserRepository;
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
    private final SpringDataTerapistaRepository terapistaRepository;
    private final UserRepository userRepository;

    public CitaService(CitaRepository citaRepository,
            ConfiguracionAgendaRepository configuracionAgendaRepository,
            DoctorRepository doctorRepository,
            SpringDataTerapistaRepository terapistaRepository,
            UserRepository userRepository) {
        this.citaRepository = citaRepository;
        this.configuracionAgendaRepository = configuracionAgendaRepository;
        this.doctorRepository = doctorRepository;
        this.terapistaRepository = terapistaRepository;
        this.userRepository = userRepository;
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
    public List<CitaDTO> obtenerMisCitas(String pacienteEmail) {
        if (pacienteEmail == null || pacienteEmail.isBlank()) {
            throw new IllegalArgumentException("El paciente no está autenticado");
        }

        // 1. Buscamos al paciente por email para obtener su nombre
        String nombrePaciente = userRepository.findByEmail(pacienteEmail)
                .map(User::getNombreCompleto)
                .orElse(pacienteEmail);

        // 2. Buscamos las citas del paciente
        return citaRepository.findByPacienteId(pacienteEmail)
                .stream()
                .map(cita -> {
                    // 3. Buscamos el nombre del médico o terapista
                    String nombreMedico = userRepository.findById(cita.getMedicoId())
                            .map(User::getNombreCompleto)
                            .orElseGet(() -> terapistaRepository.findById(cita.getMedicoId())
                                    .map(com.piedrazul.api.iam.infrastructure.entity.TerapistaJpaEntity::getNombre)
                                    .orElse("Médico #" + cita.getMedicoId()));

                    // 4. Creamos el DTO con los 8 parámetros
                    return new CitaDTO(
                            cita.getId(),
                            cita.getPacienteId(),
                            cita.getMedicoId(),
                            cita.getFecha(),
                            cita.getHora(),
                            cita.getEstado(),
                            nombrePaciente, // <-- Pasamos el nombre que hallamos arriba
                            nombreMedico // <-- Pasamos el nombre del médico/terapista
                    );
                })
                .toList();
    }

    private void validarMedicoDisponible(String medicoId) {
        // 1. Intentar buscar en la tabla de doctores
        Optional<Doctor> doctorOpt = doctorRepository.findById(medicoId);
        if (doctorOpt.isPresent()) {
            if (!doctorOpt.get().isActivo()) {
                throw new IllegalArgumentException("El medico/terapista seleccionado no esta disponible");
            }
            return; // Si es un doctor y está activo, es válido
        }

        // 2. Si no está en doctores, buscar en la tabla de terapistas
        if (terapistaRepository.findById(medicoId).isPresent()) {
            return; // Si es un terapista, también es válido
        }

        // 3. Si no existe en ninguna de las dos, lanzar el error
        throw new IllegalArgumentException("El medico/terapista seleccionado no esta disponible");
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

    // Inyecciones adicionales al constructor:
    //private final UserRepository userRepository;
    // (tu CitaRepository ya está inyectado)
    @Transactional(readOnly = true)
    public List<CitaDetalleDTO> listarDetallePorFechaYTerapista(LocalDate fecha, String medicoId) {
        // Busca las citas por el UUID del médico
        List<Cita> citas = citaRepository.findByMedicoIdAndFechaOrderByHoraAsc(medicoId, fecha);

        if (citas.isEmpty()) {
            return List.of();
        }

        // Busca el nombre del terapeuta por su UUID
        String medicoNombre = userRepository.findById(medicoId)
                .map(User::getNombreCompleto)
                .orElseGet(() -> terapistaRepository.findById(medicoId)
                        .map(com.piedrazul.api.iam.infrastructure.entity.TerapistaJpaEntity::getNombre)
                        .orElse(medicoId));

        return citas.stream().map(c -> {
            // Busca al paciente por su UUID o por email (por si acaso)
            Optional<User> pacOpt = userRepository.findById(c.getPacienteId());
            if (pacOpt.isEmpty()) {
                pacOpt = userRepository.findByEmail(c.getPacienteId());
            }

            User pac = pacOpt.orElse(null);

            return new CitaDetalleDTO(
                    c.getFecha(),
                    c.getHora(),
                    c.getPacienteId(),
                    pac != null ? pac.getNumeroIdentificacion() : "N/A",
                    pac != null ? pac.getNombreCompleto() : c.getPacienteId(),
                    c.getMedicoId(),
                    medicoNombre,
                    c.getEstado() != null ? c.getEstado().name() : ""
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<CitaDTO> listarPorCedulaPaciente(String cedula) {
        User paciente = userRepository.findByNumeroIdentificacion(cedula)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Paciente no encontrado con identificacion " + cedula));

        // Buscar las citas por UUID o por Email
        List<Cita> citas = citaRepository.findByPacienteIdOrderByFechaDescHoraDesc(paciente.getId());
        if (citas.isEmpty()) {
            citas = citaRepository.findByPacienteIdOrderByFechaDescHoraDesc(paciente.getEmail());
        }

        // Mapear a CitaDTO cruzando los nombres
        return citas.stream().map(cita -> {
            String nombreMedico = userRepository.findById(cita.getMedicoId())
                    .map(User::getNombreCompleto)
                    .orElseGet(() -> terapistaRepository.findById(cita.getMedicoId())
                            .map(com.piedrazul.api.iam.infrastructure.entity.TerapistaJpaEntity::getNombre)
                            .orElse("Médico #" + cita.getMedicoId()));

            return new CitaDTO(
                    cita.getId(),
                    cita.getPacienteId(),
                    cita.getMedicoId(),
                    cita.getFecha(),
                    cita.getHora(),
                    cita.getEstado(),
                    paciente.getNombreCompleto(), // Nombre del paciente
                    nombreMedico // Nombre del médico
            );
        }).toList();
    }
}
