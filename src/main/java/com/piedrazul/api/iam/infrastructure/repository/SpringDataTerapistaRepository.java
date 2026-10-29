package com.piedrazul.api.iam.infrastructure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.piedrazul.api.iam.infrastructure.entity.TerapistaJpaEntity;

@Repository
public interface SpringDataTerapistaRepository extends JpaRepository<TerapistaJpaEntity, String> {
    
    // Para buscar al terapista a partir del ID del usuario (el que viene en el token)
    Optional<TerapistaJpaEntity> findByUserId(String userId);
}