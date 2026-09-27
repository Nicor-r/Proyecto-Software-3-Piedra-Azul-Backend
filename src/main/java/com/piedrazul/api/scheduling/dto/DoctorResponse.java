package com.piedrazul.api.scheduling.dto;

/**
 * @file DoctorResponse.java
 * @brief DTO de salida con los datos de un doctor expuestos al cliente.
 *
 * @details
 *          Se usa para evitar exponer la entidad {@code Doctor} directamente en
 *          el
 *          JSON de respuesta. Es devuelto por {@code DoctorController#listar}.
 */
public class DoctorResponse {

    private String id;
    private String nombre;
    private boolean activo;

    public DoctorResponse() {
    }

    /**
     * @brief Crea la respuesta con los datos del doctor.
     *
     * @param id     Identificador del doctor.
     * @param nombre Nombre del doctor.
     * @param activo Indica si el doctor está activo.
     */
    public DoctorResponse(String id, String nombre, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.activo = activo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}