package com.piedrazul.api.iam.infrastructure;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.provider.PasswordHasher;
import com.piedrazul.api.iam.repository.UserRepository;

@Component
public class AgendadorAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final String agendadorEmail;
    private final String agendadorPassword;

    public AgendadorAccountInitializer(UserRepository userRepository,
                                       PasswordHasher passwordHasher,
                                       @Value("${app.agendador.email:}") String agendadorEmail,
                                       @Value("${app.agendador.password:}") String agendadorPassword) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.agendadorEmail = agendadorEmail;
        this.agendadorPassword = agendadorPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean emailConfigured = !agendadorEmail.isBlank();
        boolean passwordConfigured = !agendadorPassword.isBlank();
        if (!emailConfigured && !passwordConfigured) {
            return;
        }
        if (!emailConfigured || !passwordConfigured) {
            throw new IllegalStateException("Configure AGENDADOR_EMAIL y AGENDADOR_PASSWORD conjuntamente");
        }

        userRepository.findByEmail(agendadorEmail).ifPresentOrElse(user -> {
            if (user.getRole() != RoleEnum.AGENDADOR) {
                throw new IllegalStateException("AGENDADOR_EMAIL ya pertenece a una cuenta que no es AGENDADOR");
            }
            if (!passwordHasher.matches(agendadorPassword, user.getPassword())) {
                user.setPassword(passwordHasher.hash(agendadorPassword));
                userRepository.save(user);
            }
        }, () -> {
            User agendador = new User(UUID.randomUUID().toString(), "Agendador", agendadorEmail,
                    "AGENDADOR", "0000000000", passwordHasher.hash(agendadorPassword), RoleEnum.AGENDADOR);
            userRepository.save(agendador);
        });
    }
}
