package com.piedrazul.api.scheduling.exception;

/**
 * Excepcion de negocio del modulo scheduling.
 *
 * Se usa tanto para errores de validacion de los Value Objects del dominio
 * (ventana de agendamiento, dias de atencion, franja horaria, intervalo)
 * como para errores de negocio del servicio (medico/terapista inexistente
 * o inactivo).
 */

public class SchedulingException extends RuntimeException {
    public SchedulingException(String message) {
        super(message);
    }

    public SchedulingException(String message, Throwable cause) {
        super(message, cause);
    }
}