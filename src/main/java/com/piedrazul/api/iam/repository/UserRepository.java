package com.piedrazul.api.iam.repository;

import java.util.List;
import java.util.Optional;

import com.piedrazul.api.iam.domain.User;

/**
 * Contrato de persistencia para la entidad de dominio User. Agnostico a
 * cualquier tecnologia de base de datos (JPA, SQLite, etc.). Los Services
 * dependen UNICAMENTE de esta interfaz, nunca de su implementacion concreta.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    
    Optional<User> findFirstByEmail(String email);

    Optional<User> findByNumeroIdentificacion(String numeroIdentificacion);

    Optional<User> findById(String id);

    List<User> findAllById(List<String> ids);
}
