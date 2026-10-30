package com.piedrazul.api.iam.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.piedrazul.api.iam.infrastructure.entity.TerapistaJpaEntity;
import com.piedrazul.api.iam.infrastructure.repository.SpringDataTerapistaRepository;

@RestController
@RequestMapping("/api/terapistas")
public class TerapistaController {

    private final SpringDataTerapistaRepository terapistaRepository;

    public TerapistaController(SpringDataTerapistaRepository terapistaRepository) {
        this.terapistaRepository = terapistaRepository;
    }

    @GetMapping
    public ResponseEntity<List<TerapistaJpaEntity>> listarTerapistas() {
        return ResponseEntity.ok(terapistaRepository.findAll());
    }
}