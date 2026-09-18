package com.piedrazul.api.iam.service;

import org.springframework.stereotype.Service;

import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.dto.LoginRequest;
import com.piedrazul.api.iam.dto.TokenResponse;
import com.piedrazul.api.iam.exception.AuthException;
import com.piedrazul.api.iam.provider.PasswordHasher;
import com.piedrazul.api.iam.provider.TokenProvider;
import com.piedrazul.api.iam.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Orquesta la logica de negocio de registro y autenticacion del modulo iam.
 * Depende UNICAMENTE de las interfaces aprobadas (UserRepository,
 * PasswordHasher, TokenProvider). No conoce JPA, SQLite, BCrypt ni JJWT
 */
@Service
public class AuthService {

    /**
     * Mensaje generico usado para login. Se utiliza tanto si el usuario
     * no existe como si la contrasena es incorrecta, para no revelar
     * cual de las dos condiciones ocurrio.
     */
    private static final String INVALID_CREDENTIALS_MESSAGE = "Credenciales invalidas";

    /**
     * Mensaje usado cuando password y confirmPassword no coinciden
     * durante el registro.
     */
    private static final String PASSWORDS_DO_NOT_MATCH_MESSAGE = "Las contrasenas no coinciden";

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordHasher passwordHasher,
                       TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Registra un nuevo paciente.
     * Recibe un User parcial (tipicamente proveniente de
     * UserMapper.toDomain(RegisterRequest)), con id y role en null, y
     * confirmPassword como parametro separado (nunca forma parte de User).
     * Flujo: (1) email duplicado, (2) password == confirmPassword,
     * (3) generar UUID, (4) asignar PATIENT, (5) hashear password,
     * (6) persistir.
     * No retorna token: el paciente debe autenticarse por separado
     * mediante login().
     */
    public void registerPatient(User user, String confirmPassword) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new AuthException("El correo ya se encuentra registrado");
        }

        if (!user.getPassword().equals(confirmPassword)) {
            throw new AuthException(PASSWORDS_DO_NOT_MATCH_MESSAGE);
        }

        user.setId(UUID.randomUUID().toString());
        user.setRole(RoleEnum.PATIENT);
        user.setPassword(passwordHasher.hash(user.getPassword()));

        userRepository.save(user);
    }

    /**
     * Autentica a un usuario existente y genera su token de acceso.
     * Usa el mismo mensaje generico de AuthException tanto si el email
     * no existe como si la contrasena no coincide, para no revelar
     * informacion sobre la existencia de una cuenta.
     */
    public TokenResponse login(LoginRequest request) {
        Optional<User> foundUser = userRepository.findByEmail(request.getEmail());

        if (foundUser.isEmpty()) {
            throw new AuthException(INVALID_CREDENTIALS_MESSAGE);
        }

        User user = foundUser.get();

        if (!passwordHasher.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException(INVALID_CREDENTIALS_MESSAGE);
        }

        String token = tokenProvider.generateToken(user);

        return new TokenResponse(token);
    }
}