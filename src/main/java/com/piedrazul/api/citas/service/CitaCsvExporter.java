package com.piedrazul.api.citas.service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.piedrazul.api.citas.dto.CitaDetalleDTO;

@Component
public class CitaCsvExporter {

    private static final String[] HEADERS = {
        "Fecha", "Hora", "Identificacion", "Paciente", "Medico", "Estado"
    };

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HORA_FMT  = DateTimeFormatter.ofPattern("HH:mm");

    public byte[] exportar(List<CitaDetalleDTO> citas) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", HEADERS)).append("\n");

        for (CitaDetalleDTO c : citas) {
            sb.append(esc(c.fecha().format(FECHA_FMT))).append(",")
              .append(esc(c.hora().format(HORA_FMT))).append(",")
              .append(esc(c.pacienteIdentificacion())).append(",")
              .append(esc(c.pacienteNombre())).append(",")
              .append(esc(c.medicoNombre())).append(",")
              .append(esc(c.estado())).append("\n");
        }

        byte[] bom  = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] out  = new byte[bom.length + body.length];
        System.arraycopy(bom, 0, out, 0, bom.length);
        System.arraycopy(body, 0, out, bom.length, body.length);
        return out;
    }

    private String esc(String v) {
        if (v == null) return "";
        String s = v.replace("\"", "\"\"");
        return (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r"))
            ? "\"" + s + "\"" : s;
    }
}