package com.piedrazul.api.iam.infrastructure;

import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.infrastructure.entity.TerapistaJpaEntity;
import com.piedrazul.api.iam.infrastructure.repository.SpringDataTerapistaRepository;
import com.piedrazul.api.iam.provider.PasswordHasher;
import com.piedrazul.api.iam.repository.UserRepository;

@Component
public class TerapistaAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final SpringDataTerapistaRepository terapistaRepository;
    private final PasswordHasher passwordHasher;

    public TerapistaAccountInitializer(UserRepository userRepository,
            SpringDataTerapistaRepository terapistaRepository,
            PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.terapistaRepository = terapistaRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Terapista 1: Ana
        crearTerapista("ter-001", "terapista1@piedrazul.com", "Dra. Ana Terapista", "Terapia Física");
        // Terapista 2: Carlos
        crearTerapista("ter-002", "terapista2@piedrazul.com", "Dr. Carlos Terapista", "Fisioterapia");
    }

    private void crearTerapista(String id, String email, String nombre, String especialidad) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User(
                    UUID.randomUUID().toString(),
                    nombre,
                    email,
                    "ID-" + id,
                    "3000000000",
                    passwordHasher.hash("123456"),
                    RoleEnum.THERAPIST
            );
            userRepository.save(user);

            if (terapistaRepository.findById(id).isEmpty()) {
                TerapistaJpaEntity perfil = new TerapistaJpaEntity(id, user.getId(), nombre, especialidad);
                terapistaRepository.save(perfil);
            }
        }
    }
}
