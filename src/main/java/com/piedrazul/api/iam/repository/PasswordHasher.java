package com.piedrazul.api.iam.repository;

/**
 * Contrato que abstrae el mecanismo tecnico de hasheo y verificacion de
 * contrasenas.
 *
 * AuthService depende UNICAMENTE de esta interfaz y no conoce BCrypt
 * ni ninguna libreria de seguridad concreta.
 */
public interface PasswordHasher {

    /**
     * Genera el hash de una contrasena en texto plano.
     * La contrasena original nunca debe persistirse.
     */
    String hash(String rawPassword);

    /**
     * Verifica si una contrasena en texto plano corresponde al hash
     * almacenado.
     */
    boolean matches(String rawPassword, String hashedPassword);
}
