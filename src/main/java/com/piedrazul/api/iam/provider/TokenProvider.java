package com.piedrazul.api.iam.provider;

import com.piedrazul.api.iam.domain.User;

/**
 * Contrato que abstrae el mecanismo tecnico de generacion y validacion de
 * credenciales de sesion (tokens).
 * AuthService depende UNICAMENTE de esta interfaz y no conoce JWT, JJWT
 * ni ningun detalle de implementacion.
 * Alcance actual aprobado: unicamente las tres operaciones necesarias
 * para registro/login en este corte. No incluye getRoleFromToken().
 */
public interface TokenProvider {

    /**
     * Genera un token de acceso para el usuario autenticado.
     */
    String generateToken(User user);

    /**
     * Valida el token y determina si es sintactica y temporalmente valido.
     */
    boolean validateToken(String token);

    /**
     * Extrae el email (identidad) del usuario a partir de un token valido.
     */
    String getEmailFromToken(String token);
}
