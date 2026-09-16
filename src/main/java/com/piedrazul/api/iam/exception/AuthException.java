package com.piedrazul.api.iam.exception;

/**
 * Excepcion de negocio generica del modulo iam.
 *
 * Unica excepcion personalizada definida para este corte (sección 18 del
 * documento arquitectonico), utilizada tanto para fallos de registro
 * (ej. email ya existente) como de autenticacion (credenciales invalidas).
 *
 * El mapeo a codigos HTTP especificos se resolvera en la capa controller
 * cuando se implemente esa capa; por ahora esta clase no conoce nada de
 * HTTP, Spring Web ni codigos de estado.
 *
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
