package com.piedrazul.api.iam.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.infrastructure.entity.TerapistaJpaEntity;
import com.piedrazul.api.iam.infrastructure.repository.SpringDataTerapistaRepository;
import com.piedrazul.api.iam.provider.PasswordHasher;
import com.piedrazul.api.iam.repository.UserRepository;

import java.util.UUID;

@Component
public class TerapistaAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final SpringDataTerapistaRepository terapistaRepository;
    private final PasswordHasher passwordHasher;
    
    @Value("${app.terapista.email:}")
    private String email;
    @Value("${app.terapista.password:}")
    private String password;
    @Value("${app.terapista.id:}")
    private String terapistaId;
    @Value("${app.terapista.especialidad:}")
    private String especialidad;

    public TerapistaAccountInitializer(UserRepository userRepository, 
                                       SpringDataTerapistaRepository terapistaRepository, 
                                       PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.terapistaRepository = terapistaRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) return;

        // 1. Crear o actualizar el usuario en la tabla users
        userRepository.findFirstByEmail(email).ifPresentOrElse(user -> {
            // Ya existe
        }, () -> {
            User user = new User(
                UUID.randomUUID().toString(),
                "Terapista por Defecto",
                email,
                "ID-TER",
                "3000000000",
                passwordHasher.hash(password),
                RoleEnum.THERAPIST
            );
            userRepository.save(user);

            // 2. Crear el perfil en la tabla terapistas
            if (terapistaRepository.findById(terapistaId).isEmpty()) {
                TerapistaJpaEntity perfil = new TerapistaJpaEntity(terapistaId, user.getId(), "Terapista por Defecto", especialidad);
                terapistaRepository.save(perfil);
            }
        });
    }
}