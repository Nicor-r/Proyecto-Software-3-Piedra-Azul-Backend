package com.piedrazul.api.citas.service;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.piedrazul.api.citas.dto.FilaExportacionCita;

/**
 * Genera el archivo CSV de citas del medico (HE-04, HU-4 escenario 1):
 * columnas Hora;Paciente;Cedula;Telefono;Estado, separadas por punto y coma,
 * en UTF-8 con BOM para que Excel muestre bien tildes y ñ.
 *
 * Solo "imprime": recibe las filas ya ordenadas por hora y no consulta datos.
 */
@Component
public class ExportadorCsvCitas {

    static final String SEPARADOR = ";";
    static final String SALTO_LINEA = "\r\n";
    static final String ENCABEZADO = String.join(SEPARADOR, "Hora", "Paciente", "Cédula", "Teléfono", "Estado");

    // Marca invisible (BOM) que le indica a Excel que el archivo esta en UTF-8.
    private static final byte[] BOM_UTF8 = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    public byte[] generar(List<FilaExportacionCita> filas) {
        StringBuilder csv = new StringBuilder();
        csv.append(ENCABEZADO).append(SALTO_LINEA);

        for (FilaExportacionCita fila : filas) {
            csv.append(escapar(fila.hora() != null ? fila.hora().format(FORMATO_HORA) : null)).append(SEPARADOR)
               .append(escapar(fila.paciente())).append(SEPARADOR)
               .append(escapar(fila.cedula())).append(SEPARADOR)
               .append(escapar(fila.telefono())).append(SEPARADOR)
               .append(escapar(fila.estado() != null ? fila.estado().name() : null))
               .append(SALTO_LINEA);
        }

        byte[] contenido = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] archivo = new byte[BOM_UTF8.length + contenido.length];
        System.arraycopy(BOM_UTF8, 0, archivo, 0, BOM_UTF8.length);
        System.arraycopy(contenido, 0, archivo, BOM_UTF8.length, contenido.length);
        return archivo;
    }

    /**
     * Nombre del archivo segun HU-4: citas_<medico>_<aaaa-mm-dd>.csv.
     * El nombre del medico se simplifica (sin tildes, espacios ni simbolos)
     * para que sea un nombre de archivo valido en cualquier sistema.
     */
    public String nombreArchivo(String nombreMedico, LocalDate fecha) {
        String sinTildes = Normalizer.normalize(nombreMedico == null ? "" : nombreMedico, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String seguro = sinTildes.replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (seguro.isEmpty()) {
            seguro = "medico";
        }
        return "citas_" + seguro + "_" + fecha + ".csv";
    }

    private String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        boolean necesitaComillas = valor.contains(SEPARADOR) || valor.contains("\"")
                || valor.contains("\n") || valor.contains("\r");
        String escapado = valor.replace("\"", "\"\"");
        return necesitaComillas ? "\"" + escapado + "\"" : escapado;
    }
}