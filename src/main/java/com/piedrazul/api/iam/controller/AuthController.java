package com.piedrazul.api.iam.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.dto.LoginRequest;
import com.piedrazul.api.iam.dto.RegisterRequest;
import com.piedrazul.api.iam.dto.TokenResponse;
import com.piedrazul.api.iam.exception.AuthException;
import com.piedrazul.api.iam.infrastructure.entity.TerapistaJpaEntity;
import com.piedrazul.api.iam.infrastructure.repository.SpringDataTerapistaRepository;
import com.piedrazul.api.iam.mapper.UserMapper;
import com.piedrazul.api.iam.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;
    private final SpringDataTerapistaRepository terapistaRepository;

    public AuthController(AuthService authService, UserMapper userMapper, SpringDataTerapistaRepository terapistaRepository) {
        this.authService = authService;
        this.userMapper = userMapper;
        this.terapistaRepository = terapistaRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest request) {
        User user = userMapper.toDomain(request);
        authService.registerPatient(user, request.getConfirmPassword());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<String> handleAuthException(AuthException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @PostMapping("/register-therapist")
    public ResponseEntity<Void> registerTherapist(@RequestBody @Valid RegisterRequest request) {
        User user = userMapper.toDomain(request);
        authService.registerTherapist(user, request.getConfirmPassword());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    // En TerapistaController.java o AuthController.java

    @GetMapping("/api/terapistas")
    public ResponseEntity<List<TerapistaJpaEntity>> listarTerapistas() {
        return ResponseEntity.ok(terapistaRepository.findAll());
    }
}
