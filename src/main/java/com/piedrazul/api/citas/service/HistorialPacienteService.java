package com.piedrazul.api.citas.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.piedrazul.api.citas.domain.Cedula;
import com.piedrazul.api.citas.domain.Cita;
import com.piedrazul.api.citas.dto.CitaHistorialDTO;
import com.piedrazul.api.citas.dto.HistorialPacienteResponse;
import com.piedrazul.api.citas.infrastructure.repository.CitaRepository;
import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.repository.UserRepository;
import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

/**
 * Arma el historial de citas de un paciente a partir de su cedula (HE-05, HU-5).
 *
 * No depende de quien hace la consulta: el permiso lo controla el interceptor
 * de la ruta. Asi puede reutilizarse para otros actores (ej. terapista).
 */
@Service
public class HistorialPacienteService {

    public static final String MENSAJE_PACIENTE_NO_ENCONTRADO =
            "No existe un paciente registrado con la cédula ingresada";

    private final UserRepository userRepository;
    private final CitaRepository citaRepository;
    private final DoctorRepository doctorRepository;

    public HistorialPacienteService(UserRepository userRepository,
                                    CitaRepository citaRepository,
                                    DoctorRepository doctorRepository) {
        this.userRepository = userRepository;
        this.citaRepository = citaRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional(readOnly = true)
    public HistorialPacienteResponse consultarPorCedula(String cedulaTexto) {
        // 1. Escenarios 4 y 5: el molde valida la cedula (vacia o formato invalido).
        Cedula cedula = new Cedula(cedulaTexto);

        // 2. Escenario 3: debe existir un usuario con esa cedula Y debe ser paciente.
        User paciente = userRepository.findByNumeroIdentificacion(cedula.getValor())
                .filter(usuario -> usuario.getRole() == RoleEnum.PATIENT)
                .orElseThrow(() -> new IllegalArgumentException(MENSAJE_PACIENTE_NO_ENCONTRADO));

        // 3. Citas del paciente, de la mas reciente a la mas antigua.
        //    Decision del equipo para este corte: Cita.pacienteId guarda el CORREO del paciente.
        List<Cita> citas = citaRepository.findByPacienteIdOrderByFechaDescHoraDesc(paciente.getEmail());

        // 4. Escenario 2: paciente sin citas -> nombre y total 0.
        if (citas.isEmpty()) {
            return new HistorialPacienteResponse(cedula.getValor(), paciente.getNombreCompleto(), 0, List.of());
        }

        // 5. Escenario 1: "libreta" id -> nombre de todos los medicos, consultada UNA sola vez.
        Map<String, String> nombresMedicos = doctorRepository.findAll().stream()
                .collect(Collectors.toMap(Doctor::getId, Doctor::getNombre));

        List<CitaHistorialDTO> filas = citas.stream()
                .map(cita -> new CitaHistorialDTO(
                        cita.getFecha(),
                        cita.getHora(),
                        cita.getMedicoId(),
                        nombresMedicos.getOrDefault(cita.getMedicoId(), cita.getMedicoId()),
                        cita.getEstado()))
                .toList();

        return new HistorialPacienteResponse(cedula.getValor(), paciente.getNombreCompleto(), filas.size(), filas);
    }
}