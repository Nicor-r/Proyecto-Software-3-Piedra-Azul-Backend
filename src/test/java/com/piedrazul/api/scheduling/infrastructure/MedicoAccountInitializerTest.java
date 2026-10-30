package com.piedrazul.api.scheduling.infrastructure;

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
import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

@ExtendWith(MockitoExtension.class)
class MedicoAccountInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private DoctorRepository doctorRepository;

    private MedicoAccountInitializer crearInitializer(String email, String password, String doctorId) {
        return new MedicoAccountInitializer(userRepository, passwordHasher, doctorRepository,
                email, password, doctorId);
    }

    @Test
    void run_medicoNuevo_creaUsuarioMedicoYLoVinculaAlDoctor() {
        MedicoAccountInitializer initializer = crearInitializer("medico@test.com", "medico123", "doc-4");
        Doctor doctor = new Doctor("doc-4", "Dra. Ibis Gonzalez", true);
        when(doctorRepository.findById("doc-4")).thenReturn(Optional.of(doctor));
        when(userRepository.findByEmail("medico@test.com")).thenReturn(Optional.empty());
        when(passwordHasher.hash("medico123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        initializer.run(null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User medicoCreado = userCaptor.getValue();
        assertThat(medicoCreado.getRole()).isEqualTo(RoleEnum.MEDICO);
        assertThat(medicoCreado.getPassword()).isEqualTo("hashed-password");
        assertThat(medicoCreado.getNombreCompleto()).isEqualTo("Dra. Ibis Gonzalez");

        ArgumentCaptor<Doctor> doctorCaptor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(doctorCaptor.capture());
        assertThat(doctorCaptor.getValue().getUserId()).isEqualTo(medicoCreado.getId());
    }

    @Test
    void run_medicoYaVinculado_noGuardaNada() {
        MedicoAccountInitializer initializer = crearInitializer("medico@test.com", "medico123", "doc-4");
        Doctor doctor = new Doctor("doc-4", "Dra. Ibis Gonzalez", true, "medico-id");
        User medico = new User("medico-id", "Dra. Ibis Gonzalez", "medico@test.com", "MEDICO",
                "0000000000", "hashed-password", RoleEnum.MEDICO);
        when(doctorRepository.findById("doc-4")).thenReturn(Optional.of(doctor));
        when(userRepository.findByEmail("medico@test.com")).thenReturn(Optional.of(medico));
        when(passwordHasher.matches("medico123", "hashed-password")).thenReturn(true);

        initializer.run(null);

        verify(userRepository, never()).save(any(User.class));
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void run_doctorInexistente_noCreaUsuarioNiVinculo() {
        MedicoAccountInitializer initializer = crearInitializer("medico@test.com", "medico123", "doc-99");
        when(doctorRepository.findById("doc-99")).thenReturn(Optional.empty());

        initializer.run(null);

        verify(userRepository, never()).save(any(User.class));
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void run_configuracionIncompleta_fallaAlIniciar() {
        MedicoAccountInitializer initializer = crearInitializer("medico@test.com", "", "doc-4");

        assertThatThrownBy(() -> initializer.run(null))
                .isInstanceOf(IllegalStateException.class);

        verify(userRepository, never()).save(any(User.class));
    }
}