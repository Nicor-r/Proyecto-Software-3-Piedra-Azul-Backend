package com.piedrazul.api.iam.infrastructure.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.piedrazul.api.iam.infrastructure.entity.UserJpaEntity;
import com.piedrazul.api.iam.domain.RoleEnum;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, String> {

    Optional<UserJpaEntity> findByEmail(String email);

    Optional<UserJpaEntity> findFirstByEmail(String email);

    boolean existsByEmail(String email);

    Optional<UserJpaEntity> findByNumeroIdentificacion(String numeroIdentificacion);

    List<UserJpaEntity> findTop10ByRoleAndNumeroIdentificacionStartingWithOrderByNumeroIdentificacionAsc(
            RoleEnum role, String prefijo);
}
