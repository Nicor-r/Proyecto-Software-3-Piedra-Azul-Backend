package com.piedrazul.api.iam.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.piedrazul.api.citas.security.AuthenticatedUser;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.infrastructure.entity.TerapistaJpaEntity;
import com.piedrazul.api.iam.infrastructure.repository.SpringDataTerapistaRepository;
import com.piedrazul.api.iam.repository.UserRepository;

@RestController
@RequestMapping("/api/terapistas")
public class TerapistaController {

    private final SpringDataTerapistaRepository terapistaRepository;
    private final UserRepository userRepository;

    public TerapistaController(SpringDataTerapistaRepository terapistaRepository, UserRepository userRepository) {
        this.terapistaRepository = terapistaRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<TerapistaJpaEntity>> listarTerapistas() {
        return ResponseEntity.ok(terapistaRepository.findAll());
    }

    @GetMapping("/perfil")
    public ResponseEntity<Map<String, String>> obtenerPerfil(@AuthenticatedUser String email) {
        // 1. Buscamos el usuario por el email del token
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Buscamos su perfil de terapista
        TerapistaJpaEntity perfil = terapistaRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Perfil de terapista no encontrado"));

        // 3. Devolvemos el nombre real y su especialidad
        return ResponseEntity.ok(Map.of(
                "nombre", user.getNombreCompleto(),
                "especialidad", perfil.getEspecialidad() != null ? perfil.getEspecialidad() : "Terapista"
        ));
    }
}
