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
 * @file ConfiguracionAgendaService.java
 * @brief Servicio que orquesta la lógica de negocio de HE-03 (configuración
 *        de parámetros del sistema).
 *
 * @details
 * Depende únicamente de las interfaces {@link DoctorRepository} y
 * {@link ConfiguracionAgendaRepository}: no conoce JPA, SQLite ni que los
 * datos de doctor estén sembrados en memoria.
 *
 * Responsabilidades que asume este servicio (y que no pueden cubrir los Value
 * Objects por sí solos):
 * - Validar que el médico o terapista exista y esté activo (HU-3.1, criterio 3).
 * - Distinguir "campo ausente" ({@code null}) de "valor inválido", construyendo
 *   cada Value Object solo cuando el campo llegó.
 * - Decidir si se trata de una configuración nueva o de una actualización,
 *   dado que cada doctor tiene como máximo una {@link ConfiguracionAgenda}.
 */
@Service
public class ConfiguracionAgendaService {

    private final DoctorRepository doctorRepository;
    private final ConfiguracionAgendaRepository configuracionAgendaRepository;

    /**
     * @brief Construye el servicio con sus dependencias.
     *
     * @param doctorRepository              Repositorio de doctores.
     * @param configuracionAgendaRepository Repositorio de configuraciones de agenda.
     */
    public ConfiguracionAgendaService(DoctorRepository doctorRepository,
                                       ConfiguracionAgendaRepository configuracionAgendaRepository) {
        this.doctorRepository = doctorRepository;
        this.configuracionAgendaRepository = configuracionAgendaRepository;
    }

    /**
     * @brief Crea o actualiza la configuración de agenda de un doctor.
     *
     * @details
     * La operación es idempotente respecto al doctor: si ya existe una
     * configuración para él, se reutiliza su id y se actualiza; si no, se crea
     * una nueva. Cada campo del request se traduce al Value Object de dominio
     * correspondiente, delegando en él su validación.
     *
     * @param request Datos de entrada con la configuración a guardar.
     * @return La configuración persistida.
     * @throws SchedulingException Si el doctor no existe, está inactivo, o si
     *         algún campo obligatorio falta o tiene un valor inválido.
     */
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
     * @brief Consulta la configuración actual de un doctor.
     *
     * @details
     * Pensada para precargar el formulario de edición en el frontend. Un
     * resultado vacío no es un error: es el caso normal la primera vez que se
     * configura un doctor. El doctor, en cambio, sí debe existir y estar activo.
     *
     * @param doctorId Identificador del doctor.
     * @return {@link Optional} con la configuración si existe, o vacío en caso contrario.
     * @throws SchedulingException Si el doctor no existe o está inactivo.
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