package com.piedrazul.api.citas.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/** Verifica el índice contra una base SQLite real en memoria. */
class IndiceFranjaActivaInitializerTest {

    private JdbcTemplate jdbc;
    private IndiceFranjaActivaInitializer initializer;

    @BeforeEach
    void setUp() {
        SingleConnectionDataSource ds = new SingleConnectionDataSource("jdbc:sqlite::memory:", true);
        ds.setDriverClassName("org.sqlite.JDBC");
        jdbc = new JdbcTemplate(ds);
        jdbc.execute("CREATE TABLE citas (id INTEGER PRIMARY KEY AUTOINCREMENT, paciente_id TEXT NOT NULL, "
                + "medico_id TEXT NOT NULL, fecha DATE NOT NULL, hora TIME NOT NULL, estado TEXT NOT NULL)");
        initializer = new IndiceFranjaActivaInitializer(jdbc, ds);
        initializer.run(new DefaultApplicationArguments());
    }

    private void insertar(String estado) {
        jdbc.update("INSERT INTO citas (paciente_id, medico_id, fecha, hora, estado) "
                + "VALUES ('p', 'doc-1', '2026-12-01', '09:00:00', ?)", estado);
    }

    @Test
    void dosCitasVigentesEnLaMismaFranja_seRechazan() {
        insertar("CONFIRMADA");

        assertThatThrownBy(() -> insertar("PENDIENTE"))
                .hasMessageContaining("UNIQUE constraint failed");
    }

    @Test
    void franjaConCitaCancelada_sePuedeReservarDeNuevo() {
        insertar("CANCELADA");

        assertThatCode(() -> insertar("PENDIENTE")).doesNotThrowAnyException();
    }

    @Test
    void variasCitasCanceladasEnLaMismaFranja_sonValidas() {
        insertar("CANCELADA");
        insertar("PENDIENTE");

        assertThatCode(() -> jdbc.update("UPDATE citas SET estado = 'CANCELADA' WHERE estado = 'PENDIENTE'"))
                .doesNotThrowAnyException();
        assertThatCode(() -> insertar("CONFIRMADA")).doesNotThrowAnyException();
    }

    @Test
    void ejecutarElInicializadorDosVeces_esIdempotente() {
        assertThatCode(() -> initializer.run(new DefaultApplicationArguments())).doesNotThrowAnyException();
    }

    @Test
    void conRestriccionUnicaAntigua_avisaEnElLog() {
        SingleConnectionDataSource ds = new SingleConnectionDataSource("jdbc:sqlite::memory:", true);
        ds.setDriverClassName("org.sqlite.JDBC");
        JdbcTemplate otro = new JdbcTemplate(ds);
        otro.execute("CREATE TABLE citas (id INTEGER PRIMARY KEY, medico_id TEXT NOT NULL, fecha DATE NOT NULL, "
                + "hora TIME NOT NULL, estado TEXT NOT NULL, CONSTRAINT uk_cita_medico_fecha_hora UNIQUE (medico_id, fecha, hora))");
        Logger logger = (Logger) LoggerFactory.getLogger(IndiceFranjaActivaInitializer.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            new IndiceFranjaActivaInitializer(otro, ds).run(new DefaultApplicationArguments());
        } finally {
            logger.detachAppender(appender);
        }

        assertThat(appender.list)
                .anyMatch(e -> e.getLevel() == Level.WARN && e.getFormattedMessage().contains("restricción única antigua"));
    }

    @Test
    void sinRestriccionAntigua_noAvisa() {
        Logger logger = (Logger) LoggerFactory.getLogger(IndiceFranjaActivaInitializer.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            initializer.run(new DefaultApplicationArguments());
        } finally {
            logger.detachAppender(appender);
        }

        assertThat(appender.list).noneMatch(e -> e.getLevel() == Level.WARN);
    }
}
