package com.piedrazul.api.iam.exception;

/**
 * Excepcion de negocio generica del modulo iam.
 *
 * Unica excepcion personalizada definida para este corte ,
 * utilizada tanto para fallos de registro
 * (ej. email ya existente) como de autenticacion (credenciales invalidas).
 * Para login, el mensaje debe mantenerse generico (no debe distinguir si
 * el email existe o si la contrasena es incorrecta), para no revelar
 * informacion sobre la existencia de una cuenta.
 */
public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
