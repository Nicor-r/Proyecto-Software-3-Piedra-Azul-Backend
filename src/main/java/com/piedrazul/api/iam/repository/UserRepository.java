package com.piedrazul.api.iam.repository;

import com.piedrazul.api.iam.domain.User;

import java.util.Optional;

/**
 * Contrato de persistencia para la entidad de dominio User.
 * Agnostico a cualquier tecnologia de base de datos (JPA, SQLite, etc.).
 * Los Services dependen UNICAMENTE de esta interfaz, nunca de su
 * implementacion concreta (ver Regla de Oro, seccion 5 del documento
 * arquitectonico).
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
