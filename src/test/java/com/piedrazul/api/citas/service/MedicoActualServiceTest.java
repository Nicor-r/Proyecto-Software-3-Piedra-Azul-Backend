package com.piedrazul.api.citas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.piedrazul.api.citas.exception.UnauthorizedException;
import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.repository.UserRepository;
import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

@ExtendWith(MockitoExtension.class)
class MedicoActualServiceTest {

    private static final String EMAIL = "medico@piedrazul.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorRepository doctorRepository;

    private MedicoActualService service;
    private User usuarioMedico;

    @BeforeEach
    void setUp() {
        service = new MedicoActualService(userRepository, doctorRepository);
        usuarioMedico = new User("user-123", "Dra. Ibis Gonzalez", EMAIL, "MEDICO",
                "0000000000", "hashed-password", RoleEnum.MEDICO);
    }

    @Test
    void obtenerMedicoActual_medicoVinculadoYActivo_retornaSuDoctor() {
        Doctor doc4 = new Doctor("doc-4", "Dra. Ibis Gonzalez", true, "user-123");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioMedico));
        when(doctorRepository.findByUserId("user-123")).thenReturn(Optional.of(doc4));

        Doctor resultado = service.obtenerMedicoActual(EMAIL);

        assertThat(resultado.getId()).isEqualTo("doc-4");
    }

    @Test
    void obtenerMedicoActual_emailVacio_lanzaUnauthorizedSinConsultar() {
        assertThatThrownBy(() -> service.obtenerMedicoActual(" "))
                .isInstanceOf(UnauthorizedException.class);

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void obtenerMedicoActual_usuarioInexistente_lanzaUnauthorized() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerMedicoActual(EMAIL))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void obtenerMedicoActual_usuarioSinFicha_lanzaMensajeSinFicha() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioMedico));
        when(doctorRepository.findByUserId("user-123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerMedicoActual(EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MedicoActualService.MENSAJE_SIN_FICHA);
    }

    @Test
    void obtenerMedicoActual_fichaInactiva_lanzaMensajeMedicoNoDisponible() {
        Doctor inactivo = new Doctor("doc-4", "Dra. Ibis Gonzalez", false, "user-123");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioMedico));
        when(doctorRepository.findByUserId("user-123")).thenReturn(Optional.of(inactivo));

        assertThatThrownBy(() -> service.obtenerMedicoActual(EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MedicoActualService.MENSAJE_MEDICO_NO_DISPONIBLE);
    }
}