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
public class AdminAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final String adminEmail;
    private final String adminPassword;

    public AdminAccountInitializer(UserRepository userRepository,
                                   PasswordHasher passwordHasher,
                                   @Value("${app.admin.email:}") String adminEmail,
                                   @Value("${app.admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean emailConfigured = !adminEmail.isBlank();
        boolean passwordConfigured = !adminPassword.isBlank();
        if (!emailConfigured && !passwordConfigured) {
            return;
        }
        if (!emailConfigured || !passwordConfigured) {
            throw new IllegalStateException("Configure ADMIN_EMAIL y ADMIN_PASSWORD conjuntamente");
        }

        userRepository.findByEmail(adminEmail).ifPresentOrElse(user -> {
            if (user.getRole() != RoleEnum.ADMIN) {
                throw new IllegalStateException("ADMIN_EMAIL ya pertenece a una cuenta que no es ADMIN");
            }
            if (!passwordHasher.matches(adminPassword, user.getPassword())) {
                user.setPassword(passwordHasher.hash(adminPassword));
                userRepository.save(user);
            }
        }, () -> {
            User admin = new User(UUID.randomUUID().toString(), "Administrador", adminEmail,
                    "ADMIN", "0000000000", passwordHasher.hash(adminPassword), RoleEnum.ADMIN);
            userRepository.save(admin);
        });
    }
}