package com.piedrazul.api.citas.service;

import org.springframework.stereotype.Service;

import com.piedrazul.api.citas.exception.UnauthorizedException;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.repository.UserRepository;
import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

/**
 * Responde la pregunta "¿que medico soy?" a partir del correo del usuario
 * autenticado (el que viene en el token).
 *
 * Flujo: correo -> User (modulo iam) -> Doctor vinculado por userId (modulo
 * scheduling).
 *
 * Lo reutilizan las funcionalidades del actor MEDICO de HE-04 (exportar sus
 * citas) y HE-05 (historial de pacientes). Solo depende de interfaces de los
 * otros modulos (UserRepository, DoctorRepository).
 */
@Service
public class MedicoActualService {

    static final String MENSAJE_MEDICO_NO_DISPONIBLE = "El médico/terapista seleccionado no está disponible";
    static final String MENSAJE_SIN_FICHA = "El usuario no tiene una ficha de médico asignada";

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    public MedicoActualService(UserRepository userRepository, DoctorRepository doctorRepository) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
    }

    public Doctor obtenerMedicoActual(String email) {
        // 1. Sin correo no sabemos quien es: no esta autenticado.
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Falta el token de autenticacion");
        }

        // 2. ¿Quien tiene este correo?
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));

        // 3. ¿Que ficha de medico le pertenece a ese usuario?
        Doctor doctor = doctorRepository.findByUserId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException(MENSAJE_SIN_FICHA));

        // 4. HU-4 escenario 4: el medico debe estar activo.
        if (!doctor.isActivo()) {
            throw new IllegalArgumentException(MENSAJE_MEDICO_NO_DISPONIBLE);
        }

        return doctor;
    }
}