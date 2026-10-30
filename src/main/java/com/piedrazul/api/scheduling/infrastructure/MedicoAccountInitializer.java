package com.piedrazul.api.scheduling.infrastructure;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.provider.PasswordHasher;
import com.piedrazul.api.iam.repository.UserRepository;
import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

/**
 * @file MedicoAccountInitializer.java
 * @brief Crea (si no existe) la cuenta de un medico de prueba y la vincula a su
 *        ficha de Doctor, para poder probar HE-04 y HE-05 con el rol MEDICO.
 *
 * @details
 * Se ejecuta DESPUES de DoctorDataInitializer (@Order 2 vs @Order 1), porque
 * necesita que la ficha del doctor ya exista. Solo usa las interfaces del
 * modulo iam (UserRepository, PasswordHasher), nunca sus implementaciones.
 */
@Component
@Order(2)
public class MedicoAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MedicoAccountInitializer.class);

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final DoctorRepository doctorRepository;
    private final String medicoEmail;
    private final String medicoPassword;
    private final String medicoDoctorId;

    public MedicoAccountInitializer(UserRepository userRepository,
                                    PasswordHasher passwordHasher,
                                    DoctorRepository doctorRepository,
                                    @Value("${app.medico.email:}") String medicoEmail,
                                    @Value("${app.medico.password:}") String medicoPassword,
                                    @Value("${app.medico.doctor-id:}") String medicoDoctorId) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.doctorRepository = doctorRepository;
        this.medicoEmail = medicoEmail;
        this.medicoPassword = medicoPassword;
        this.medicoDoctorId = medicoDoctorId;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean emailConfigurado = !medicoEmail.isBlank();
        boolean passwordConfigurado = !medicoPassword.isBlank();
        boolean doctorConfigurado = !medicoDoctorId.isBlank();

        // 1. Si no se configuro nada, no se crea ningun medico de prueba.
        if (!emailConfigurado && !passwordConfigurado && !doctorConfigurado) {
            return;
        }
        // 2. Configuracion a medias = error de configuracion.
        if (!emailConfigurado || !passwordConfigurado || !doctorConfigurado) {
            throw new IllegalStateException(
                    "Configure MEDICO_EMAIL, MEDICO_PASSWORD y MEDICO_DOCTOR_ID conjuntamente");
        }

        // 3. La ficha del doctor debe existir.
        Doctor doctor = doctorRepository.findById(medicoDoctorId).orElse(null);
        if (doctor == null) {
            log.warn("No existe el doctor {}; no se creo el medico de prueba", medicoDoctorId);
            return;
        }

        // 4. Obtener (o crear) el carne del medico.
        User medico = obtenerOCrearUsuarioMedico(doctor);

        // 5. Escribir en la ficha quien es su dueño (solo si aun no lo dice).
        if (!medico.getId().equals(doctor.getUserId())) {
            doctor.setUserId(medico.getId());
            doctorRepository.save(doctor);
            log.info("Medico de prueba {} vinculado al doctor {}", medicoEmail, doctor.getId());
        }
    }

    private User obtenerOCrearUsuarioMedico(Doctor doctor) {
        User existente = userRepository.findByEmail(medicoEmail).orElse(null);

        if (existente == null) {
            User nuevo = new User(UUID.randomUUID().toString(), doctor.getNombre(), medicoEmail,
                    "MEDICO", "0000000000", passwordHasher.hash(medicoPassword), RoleEnum.MEDICO);
            return userRepository.save(nuevo);
        }

        if (existente.getRole() != RoleEnum.MEDICO) {
            throw new IllegalStateException("MEDICO_EMAIL ya pertenece a una cuenta que no es MEDICO");
        }
        if (!passwordHasher.matches(medicoPassword, existente.getPassword())) {
            existente.setPassword(passwordHasher.hash(medicoPassword));
            userRepository.save(existente);
        }
        return existente;
    }
}