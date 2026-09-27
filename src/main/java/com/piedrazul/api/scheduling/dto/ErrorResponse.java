package com.piedrazul.api.scheduling.dto;

/**
 * @file ErrorResponse.java
 * @brief DTO que estandariza el cuerpo de las respuestas de error del módulo.
 *
 * @details
 *          Sustituye las respuestas en texto plano por un objeto JSON con un
 *          campo
 *          {@code message}. Así el frontend puede invocar siempre
 *          {@code response.json()}
 *          sin distinguir entre respuestas exitosas y de error. Es devuelto,
 *          entre otros,
 *          por {@code ConfiguracionAgendaController#handleSchedulingException}.
 */
public class ErrorResponse {

    private String message;

    public ErrorResponse() {
    }

    /**
     * @brief Crea la respuesta de error con el mensaje indicado.
     *
     * @param message Descripción del error mostrada al cliente.
     */
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