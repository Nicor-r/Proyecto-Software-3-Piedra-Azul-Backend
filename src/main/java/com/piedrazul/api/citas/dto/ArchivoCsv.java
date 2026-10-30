package com.piedrazul.api.citas.dto;

/**
 * Archivo CSV generado: el nombre con el que se descarga y su contenido en bytes.
 */
public record ArchivoCsv(String nombreArchivo, byte[] contenido) {
}