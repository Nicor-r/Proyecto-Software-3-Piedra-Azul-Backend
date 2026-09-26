package com.piedrazul.api.scheduling.service;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.piedrazul.api.scheduling.domain.ConfiguracionAgenda;
import com.piedrazul.api.scheduling.domain.DiasAtencion;
import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.domain.FranjaHoraria;
import com.piedrazul.api.scheduling.domain.IntervaloCitas;
import com.piedrazul.api.scheduling.domain.VentanaAgendamiento;
import com.piedrazul.api.scheduling.dto.ConfiguracionAgendaRequest;
import com.piedrazul.api.scheduling.exception.SchedulingException;
import com.piedrazul.api.scheduling.repository.ConfiguracionAgendaRepository;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

/**
 * Orquesta la logica de negocio de HE-03 (configuracion de parametros del
 * sistema).
 *
 * Depende UNICAMENTE de las interfaces DoctorRepository y
 * ConfiguracionAgendaRepository. No conoce JPA, SQLite, ni que los datos
 * de Doctor esten quemados.
 *
 * Responsabilidad de este Service (lo que NO hacen los Value Objects):
 *  1. Validar que el medico/terapista exista y este activo (HU-3.1,
 *     criterio 3).
 *  2. Distinguir "campo vacio" (null) de "valor invalido", construyendo
 *     los Value Objects solo cuando el campo si llego.
 *  3. Decidir si es una configuracion nueva o una actualizacion (un
 *     medico/terapista tiene una unica ConfiguracionAgenda).
 */
@Service
public class ConfiguracionAgendaService {

    private final DoctorRepository doctorRepository;
    private final ConfiguracionAgendaRepository configuracionAgendaRepository;

    public ConfiguracionAgendaService(DoctorRepository doctorRepository,
                                       ConfiguracionAgendaRepository configuracionAgendaRepository) {
        this.doctorRepository = doctorRepository;
        this.configuracionAgendaRepository = configuracionAgendaRepository;
    }

    public ConfiguracionAgenda configurarAgenda(ConfiguracionAgendaRequest request) {
        Doctor doctor = obtenerDoctorValidoYActivo(request.getDoctorId());

        if (request.getVentanaSemanas() == null) {
            throw new SchedulingException("El campo es obligatorio: ventana de agendamiento");
        }
        VentanaAgendamiento ventana = new VentanaAgendamiento(request.getVentanaSemanas());

        Set<DayOfWeek> diasSeleccionados = request.getDiasAtencion() == null
                ? Collections.emptySet()
                : request.getDiasAtencion();
        DiasAtencion dias = new DiasAtencion(diasSeleccionados);

        FranjaHoraria franja = new FranjaHoraria(request.getHoraInicio(), request.getHoraFin());

        if (request.getIntervaloMinutos() == null) {
            throw new SchedulingException("El campo es obligatorio: intervalo de tiempo");
        }
        IntervaloCitas intervalo = new IntervaloCitas(request.getIntervaloMinutos());

        String idExistente = configuracionAgendaRepository.findByDoctorId(doctor.getId())
                .map(ConfiguracionAgenda::getId)
                .orElse(null);

        ConfiguracionAgenda configuracion = new ConfiguracionAgenda(
                idExistente, doctor.getId(), ventana, dias, franja, intervalo);

        return configuracionAgendaRepository.save(configuracion);
    }

    /**
     * Consulta la configuracion actual de un doctor (para precargar el
     * formulario de edicion en el frontend). Devuelve Optional.empty() si
     * el doctor aun no tiene configuracion guardada (no es un error: es un
     * caso normal la primera vez que se configura un doctor).
     */
    public Optional<ConfiguracionAgenda> consultarConfiguracion(String doctorId) {
        obtenerDoctorValidoYActivo(doctorId);
        return configuracionAgendaRepository.findByDoctorId(doctorId);
    }

    private Doctor obtenerDoctorValidoYActivo(String doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new SchedulingException("Debe seleccionar un medico/terapista valido"));

        if (!doctor.isActivo()) {
            throw new SchedulingException("Debe seleccionar un medico/terapista valido");
        }

        return doctor;
    }
}
