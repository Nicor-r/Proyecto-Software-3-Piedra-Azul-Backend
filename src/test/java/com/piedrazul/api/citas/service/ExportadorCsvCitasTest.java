package com.piedrazul.api.citas.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.piedrazul.api.citas.domain.EstadoCita;
import com.piedrazul.api.citas.dto.FilaExportacionCita;

class ExportadorCsvCitasTest {

    private final ExportadorCsvCitas exportador = new ExportadorCsvCitas();

    /** Convierte los bytes del archivo en texto, saltando los 3 bytes del BOM. */
    private String contenidoSinBom(byte[] archivo) {
        return new String(archivo, 3, archivo.length - 3, StandardCharsets.UTF_8);
    }

    @Test
    void generar_empiezaConLaMarcaBomDeUtf8() {
        byte[] archivo = exportador.generar(List.of());

        assertThat(Arrays.copyOfRange(archivo, 0, 3))
                .containsExactly((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
    }

    @Test
    void generar_primeraLineaEsElEncabezadoConLasColumnasDeHU4() {
        String csv = contenidoSinBom(exportador.generar(List.of()));

        assertThat(csv).startsWith("Hora;Paciente;Cédula;Teléfono;Estado\r\n");
    }

    @Test
    void generar_escribeUnaLineaPorCitaSeparadaPorPuntoYComa() {
        List<FilaExportacionCita> filas = List.of(
                new FilaExportacionCita(LocalTime.of(8, 0), "Ana Ruiz", "1061000001", "3001112233", EstadoCita.CONFIRMADA),
                new FilaExportacionCita(LocalTime.of(9, 0), "Carlos Medina", "1061000003", "3003334455", EstadoCita.PENDIENTE));

        String[] lineas = contenidoSinBom(exportador.generar(filas)).split("\r\n");

        assertThat(lineas).containsExactly(
                "Hora;Paciente;Cédula;Teléfono;Estado",
                "08:00;Ana Ruiz;1061000001;3001112233;CONFIRMADA",
                "09:00;Carlos Medina;1061000003;3003334455;PENDIENTE");
    }

    @Test
    void generar_conservaTildesYEnie() {
        List<FilaExportacionCita> filas = List.of(
                new FilaExportacionCita(LocalTime.of(10, 30), "Íñigo Muñoz", "1061000020", "3000000000", EstadoCita.CONFIRMADA));

        String csv = contenidoSinBom(exportador.generar(filas));

        assertThat(csv).contains("Íñigo Muñoz");
    }

    @Test
    void generar_valorConPuntoYComa_seEncierraEnComillas() {
        List<FilaExportacionCita> filas = List.of(
                new FilaExportacionCita(LocalTime.of(8, 0), "Ruiz; Ana", "1061000001", "3001112233", EstadoCita.CONFIRMADA));

        String csv = contenidoSinBom(exportador.generar(filas));

        assertThat(csv).contains("08:00;\"Ruiz; Ana\";1061000001");
    }

    @Test
    void generar_valorConComillas_lasDuplicaYLoEncierra() {
        List<FilaExportacionCita> filas = List.of(
                new FilaExportacionCita(LocalTime.of(8, 0), "Ana \"La Doc\"", "1061000001", "3001112233", EstadoCita.CONFIRMADA));

        String csv = contenidoSinBom(exportador.generar(filas));

        assertThat(csv).contains(";\"Ana \"\"La Doc\"\"\";");
    }

    @Test
    void generar_valoresNulos_quedanVacios() {
        List<FilaExportacionCita> filas = List.of(
                new FilaExportacionCita(LocalTime.of(8, 0), "Ana Ruiz", "1061000001", null, EstadoCita.CONFIRMADA));

        String[] lineas = contenidoSinBom(exportador.generar(filas)).split("\r\n");

        assertThat(lineas[1]).isEqualTo("08:00;Ana Ruiz;1061000001;;CONFIRMADA");
    }

    @Test
    void nombreArchivo_quitaTildesEspaciosYSimbolosDelNombreDelMedico() {
        String nombre = exportador.nombreArchivo("Dra. Ibis González - Cardiología", LocalDate.of(2026, 10, 30));

        assertThat(nombre).isEqualTo("citas_Dra_Ibis_Gonzalez_Cardiologia_2026-10-30.csv");
    }

    @Test
    void nombreArchivo_sinNombreDeMedico_usaMedicoPorDefecto() {
        String nombre = exportador.nombreArchivo(null, LocalDate.of(2026, 11, 5));

        assertThat(nombre).isEqualTo("citas_medico_2026-11-05.csv");
    }
}