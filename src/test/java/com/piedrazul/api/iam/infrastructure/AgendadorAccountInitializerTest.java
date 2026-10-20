package com.piedrazul.api.iam.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.provider.PasswordHasher;
import com.piedrazul.api.iam.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AgendadorAccountInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Test
    void run_conCredencialesNuevas_creaAgendadorConPasswordHasheada() {
        AgendadorAccountInitializer initializer = new AgendadorAccountInitializer(userRepository, passwordHasher,
                "agendador@test.com", "secret-password");
        when(userRepository.findByEmail("agendador@test.com")).thenReturn(Optional.empty());
        when(passwordHasher.hash("secret-password")).thenReturn("hashed-password");

        initializer.run(null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(RoleEnum.AGENDADOR);
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("hashed-password");
    }

    @Test
    void run_soloUnaCredencialConfigurada_fallaAlIniciar() {
        AgendadorAccountInitializer initializer = new AgendadorAccountInitializer(userRepository, passwordHasher,
                "agendador@test.com", "");

        assertThatThrownBy(() -> initializer.run(null))
                .isInstanceOf(IllegalStateException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_agendadorExistente_actualizaPasswordCuandoCambia() {
        AgendadorAccountInitializer initializer = new AgendadorAccountInitializer(userRepository, passwordHasher,
                "agendador@test.com", "new-password");
        User existingAgendador = new User("agendador-id", "Agendador", "agendador@test.com", "AGENDADOR",
                "0000000000", "old-hash", RoleEnum.AGENDADOR);
        when(userRepository.findByEmail("agendador@test.com")).thenReturn(Optional.of(existingAgendador));
        when(passwordHasher.matches("new-password", "old-hash")).thenReturn(false);
        when(passwordHasher.hash("new-password")).thenReturn("new-hash");

        initializer.run(null);

        assertThat(existingAgendador.getPassword()).isEqualTo("new-hash");
        verify(userRepository).save(existingAgendador);
    }

    @Test
    void run_sinCredencialesConfiguradas_noHaceNada() {
        AgendadorAccountInitializer initializer = new AgendadorAccountInitializer(userRepository, passwordHasher,
                "", "");

        initializer.run(null);

        verify(userRepository, never()).save(any(User.class));
    }
}
