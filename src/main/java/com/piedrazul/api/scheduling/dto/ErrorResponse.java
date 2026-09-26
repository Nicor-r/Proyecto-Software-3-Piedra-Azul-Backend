package com.piedrazul.api.scheduling.dto;

/**
 * Forma consistente para todas las respuestas de error de este modulo.
 * Antes se devolvia texto plano; con esto el frontend siempre puede hacer
 * response.json() sin que se rompa, sea exito o error.
 */
public class ErrorResponse {

    private String message;

    public ErrorResponse() {
    }

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
