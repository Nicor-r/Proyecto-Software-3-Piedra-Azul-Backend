package com.piedrazul.api.citas.infrastructure;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Garantiza a nivel de base de datos que un médico no tenga dos citas vigentes en la misma
 * franja, pero permite reutilizar una franja cuya cita fue cancelada (RF6).
 *
 * <p>Se usa un índice único parcial ({@code WHERE estado <> 'CANCELADA'}), con la misma
 * sintaxis en SQLite y PostgreSQL. Hibernate no puede expresarlo con anotaciones, por eso
 * se crea aquí al arrancar, una vez que {@code ddl-auto} ya generó la tabla.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IndiceFranjaActivaInitializer implements ApplicationRunner {

    static final String NOMBRE_INDICE = "uk_cita_franja_activa";

    static final String SQL_CREAR_INDICE =
            "CREATE UNIQUE INDEX IF NOT EXISTS " + NOMBRE_INDICE
                    + " ON citas (medico_id, fecha, hora) WHERE estado <> 'CANCELADA'";

    private static final Logger log = LoggerFactory.getLogger(IndiceFranjaActivaInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public IndiceFranjaActivaInitializer(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute(SQL_CREAR_INDICE);
            log.info("Índice único parcial {} verificado", NOMBRE_INDICE);
        } catch (RuntimeException e) {
            log.error("No se pudo crear el índice {}: {}", NOMBRE_INDICE, e.getMessage());
            return;
        }
        avisarSiPersisteRestriccionAntigua();
    }

    /**
     * Las bases creadas antes de RF6 conservan una restricción única total sobre
     * (medico_id, fecha, hora) que impediría reservar una franja cancelada.
     */
    private void avisarSiPersisteRestriccionAntigua() {
        try (Connection conexion = dataSource.getConnection();
                ResultSet rs = conexion.getMetaData().getIndexInfo(null, null, "citas", true, false)) {
            Map<String, List<String>> columnasPorIndice = new TreeMap<>();
            while (rs.next()) {
                String indice = rs.getString("INDEX_NAME");
                String columna = rs.getString("COLUMN_NAME");
                if (indice != null && columna != null && !NOMBRE_INDICE.equalsIgnoreCase(indice)) {
                    columnasPorIndice.computeIfAbsent(indice, k -> new ArrayList<>()).add(columna.toLowerCase());
                }
            }
            columnasPorIndice.forEach((indice, columnas) -> {
                if (columnas.size() == 3 && columnas.containsAll(List.of("medico_id", "fecha", "hora"))) {
                    log.warn("La tabla citas conserva la restricción única antigua '{}' sobre (medico_id, fecha, hora): "
                            + "una franja cancelada no podrá volver a reservarse. Elimine la restricción o regenere la base de datos.",
                            indice);
                }
            });
        } catch (SQLException | RuntimeException e) {
            log.debug("No se pudo inspeccionar los índices de citas: {}", e.getMessage());
        }
    }
}
