package com.piedrazul.api.scheduling.exception;

/**
 * @file SchedulingException.java
 * @brief Excepción de negocio del módulo scheduling.
 *
 * @details
 *          Se usa tanto para errores de validación de los Value Objects del
 *          dominio
 *          (ventana de agendamiento, días de atención, franja horaria,
 *          intervalo) como
 *          para errores de negocio del servicio (médico o terapista inexistente
 *          o
 *          inactivo). Al extender {@link RuntimeException} no obliga a
 *          declararla en
 *          las firmas de los métodos.
 */
public class SchedulingException extends RuntimeException {

    /**
     * @brief Crea la excepción con un mensaje descriptivo.
     *
     * @param message Descripción del error.
     */
    public SchedulingException(String message) {
        super(message);
    }

    /**
     * @brief Crea la excepción con un mensaje y la causa subyacente.
     *
     * @param message Descripción del error.
     * @param cause   Excepción original que provocó este error.
     */
    public SchedulingException(String message, Throwable cause) {
        super(message, cause);
    }
}