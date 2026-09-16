package com.piedrazul.api.iam.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.dto.LoginRequest;
import com.piedrazul.api.iam.dto.TokenResponse;
import com.piedrazul.api.iam.exception.AuthException;
import com.piedrazul.api.iam.repository.PasswordHasher;
import com.piedrazul.api.iam.repository.TokenProvider;
import com.piedrazul.api.iam.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String CREDENCIALES_INVALIDAS = "Credenciales invalidas";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenProvider tokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordHasher, tokenProvider);
    }

    // ===================== registerPatient() =====================

    @Test
    void registerPatient_correoNuevo_registraCorrectamente() {
        User newUser = new User(null, "nuevo@test.com", "rawPassword123", null);

        when(userRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(passwordHasher.hash("rawPassword123")).thenReturn("hashedPassword123");

        authService.registerPatient(newUser);

        verify(userRepository, times(1)).existsByEmail("nuevo@test.com");
        verify(passwordHasher, times(1)).hash("rawPassword123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerPatient_correoYaRegistrado_lanzaAuthException() {
        User existingEmailUser = new User(null, "existente@test.com", "rawPassword123", null);

        when(userRepository.existsByEmail("existente@test.com")).thenReturn(true);

        AuthException exception = assertThrows(AuthException.class,
                () -> authService.registerPatient(existingEmailUser));

        assertThat(exception.getMessage()).isEqualTo("El correo ya se encuentra registrado");

        verify(userRepository, never()).save(any(User.class));
        verify(passwordHasher, never()).hash(anyString());
    }

    @Test
    void registerPatient_generaIdParaElUsuario() {
        User newUser = new User(null, "nuevo@test.com", "rawPassword123", null);

        when(userRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(passwordHasher.hash("rawPassword123")).thenReturn("hashedPassword123");

        authService.registerPatient(newUser);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getId()).isNotBlank();
    }

    @Test
    void registerPatient_asignaRolPatient() {
        User newUser = new User(null, "nuevo@test.com", "rawPassword123", null);

        when(userRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(passwordHasher.hash("rawPassword123")).thenReturn("hashedPassword123");

        authService.registerPatient(newUser);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).isEqualTo(RoleEnum.PATIENT);
    }

    @Test
    void registerPatient_pasaPasswordOriginalAPasswordHasher() {
        User newUser = new User(null, "nuevo@test.com", "rawPassword123", null);

        when(userRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(passwordHasher.hash("rawPassword123")).thenReturn("hashedPassword123");

        authService.registerPatient(newUser);

        verify(passwordHasher, times(1)).hash("rawPassword123");
    }

    @Test
    void registerPatient_guardaUsuarioConPasswordHasheado() {
        User newUser = new User(null, "nuevo@test.com", "rawPassword123", null);

        when(userRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(passwordHasher.hash("rawPassword123")).thenReturn("hashedPassword123");

        authService.registerPatient(newUser);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("hashedPassword123");
    }

    // ===================== login() =====================

    @Test
    void login_credencialesCorrectas_retornaTokenResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("usuario@test.com");
        request.setPassword("rawPassword123");

        User existingUser = new User("user-id-1", "usuario@test.com", "hashedPassword123", RoleEnum.PATIENT);

        when(userRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(existingUser));
        when(passwordHasher.matches("rawPassword123", "hashedPassword123")).thenReturn(true);
        when(tokenProvider.generateToken(existingUser)).thenReturn("generated-jwt-token");

        TokenResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("generated-jwt-token");
    }

    @Test
    void login_correoInexistente_lanzaAuthExceptionCredencialesInvalidas() {
        LoginRequest request = new LoginRequest();
        request.setEmail("inexistente@test.com");
        request.setPassword("rawPassword123");

        when(userRepository.findByEmail("inexistente@test.com")).thenReturn(Optional.empty());

        AuthException exception = assertThrows(AuthException.class,
                () -> authService.login(request));

        assertThat(exception.getMessage()).isEqualTo(CREDENCIALES_INVALIDAS);

        verify(tokenProvider, never()).generateToken(any(User.class));
        verify(passwordHasher, never()).matches(anyString(), anyString());
    }

    @Test
    void login_passwordIncorrecta_lanzaAuthExceptionCredencialesInvalidas() {
        LoginRequest request = new LoginRequest();
        request.setEmail("usuario@test.com");
        request.setPassword("wrongPassword");

        User existingUser = new User("user-id-1", "usuario@test.com", "hashedPassword123", RoleEnum.PATIENT);

        when(userRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(existingUser));
        when(passwordHasher.matches("wrongPassword", "hashedPassword123")).thenReturn(false);

        AuthException exception = assertThrows(AuthException.class,
                () -> authService.login(request));

        assertThat(exception.getMessage()).isEqualTo(CREDENCIALES_INVALIDAS);

        verify(tokenProvider, never()).generateToken(any(User.class));
    }

    @Test
    void login_credencialesCorrectas_invocaPasswordHasherMatches() {
        LoginRequest request = new LoginRequest();
        request.setEmail("usuario@test.com");
        request.setPassword("rawPassword123");

        User existingUser = new User("user-id-1", "usuario@test.com", "hashedPassword123", RoleEnum.PATIENT);

        when(userRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(existingUser));
        when(passwordHasher.matches("rawPassword123", "hashedPassword123")).thenReturn(true);
        when(tokenProvider.generateToken(existingUser)).thenReturn("generated-jwt-token");

        authService.login(request);

        verify(passwordHasher, times(1)).matches("rawPassword123", "hashedPassword123");
    }

    @Test
    void login_credencialesCorrectas_invocaTokenProviderGenerateToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("usuario@test.com");
        request.setPassword("rawPassword123");

        User existingUser = new User("user-id-1", "usuario@test.com", "hashedPassword123", RoleEnum.PATIENT);

        when(userRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(existingUser));
        when(passwordHasher.matches("rawPassword123", "hashedPassword123")).thenReturn(true);
        when(tokenProvider.generateToken(existingUser)).thenReturn("generated-jwt-token");

        authService.login(request);

        verify(tokenProvider, times(1)).generateToken(existingUser);
    }

    @Test
    void login_credencialesInvalidas_noGeneraToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("usuario@test.com");
        request.setPassword("wrongPassword");

        User existingUser = new User("user-id-1", "usuario@test.com", "hashedPassword123", RoleEnum.PATIENT);

        when(userRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(existingUser));
        when(passwordHasher.matches(eq("wrongPassword"), eq("hashedPassword123"))).thenReturn(false);

        assertThrows(AuthException.class, () -> authService.login(request));

        verify(tokenProvider, never()).generateToken(any(User.class));
    }
}