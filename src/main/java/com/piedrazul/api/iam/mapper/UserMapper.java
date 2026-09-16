package com.piedrazul.api.iam.mapper;

import org.springframework.stereotype.Component;

import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.dto.RegisterRequest;

/**
 * Mapper puro de transformaciones DTO <-> Domain para el modulo iam.
 *
 * Responsabilidad EXCLUSIVA: transformar estructuras de datos.
 * NO contiene validaciones, logica de negocio, llamadas a repositorios
 * ni logica de hashing (BCrypt) o tokens (JWT).
 *
 * Nota de diseno (pendiente de tu confirmacion, ver mensaje adjunto):
 * este mapper NO asigna el RoleEnum ni genera el id (UUID) del User,
 * ya que decidir el rol por defecto de un registro y generar la
 * identidad del usuario se consideran aqui responsabilidades de
 * AuthService (capa service), no de esta capa de mapeo puro.
 */
@Component
public class UserMapper {

    /**
     * Transforma un RegisterRequest en un User de dominio parcial.
     * El id y el role quedan sin asignar (null): AuthService es quien
     * completa esos valores antes de invocar UserRepository.save().
     * El password se copia tal cual (texto plano) porque el hashing
     * es responsabilidad de PasswordHasher en la capa service, no del mapper.
     */
    public User toDomain(RegisterRequest request) {
        if (request == null) {
            return null;
        }
        return new User(
                null,
                request.getEmail(),
                request.getPassword(),
                null
        );
    }
}