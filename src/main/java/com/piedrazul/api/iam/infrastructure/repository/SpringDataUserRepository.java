package com.piedrazul.api.iam.infrastructure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.piedrazul.api.iam.infrastructure.entity.UserJpaEntity;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, String> {

    Optional<UserJpaEntity> findByEmail(String email);

    Optional<UserJpaEntity> findFirstByEmail(String email);

    boolean existsByEmail(String email);

    Optional<UserJpaEntity> findByNumeroIdentificacion(String numeroIdentificacion);
}
