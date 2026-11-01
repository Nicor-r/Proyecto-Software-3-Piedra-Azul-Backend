package com.piedrazul.api.iam.infrastructure;

import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.provider.PasswordHasher;
import com.piedrazul.api.iam.repository.UserRepository;

@Component
public class PacienteAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public PacienteAccountInitializer(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void run(ApplicationArguments args) {
        String email = "paciente@test.com";
        
        if (userRepository.findByEmail(email).isEmpty()) {
            User paciente = new User(
                    UUID.randomUUID().toString(),
                    "Paciente Test",
                    email,
                    "1234567890", // Cédula de prueba
                    "3001234567", // Teléfono
                    passwordHasher.hash("123456"), // Contraseña
                    RoleEnum.PATIENT
            );
            userRepository.save(paciente);
            System.out.println("Paciente Test inicializado: " + email + " / 123456");
        }
    }
}