package com.piedrazul.api.scheduling.service;

import java.time.DayOfWeek;
import java.util.Collections;
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

    private Doctor obtenerDoctorValidoYActivo(String doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new SchedulingException("Debe seleccionar un medico/terapista valido"));

        if (!doctor.isActivo()) {
            throw new SchedulingException("Debe seleccionar un medico/terapista valido");
        }

        return doctor;
    }
}
