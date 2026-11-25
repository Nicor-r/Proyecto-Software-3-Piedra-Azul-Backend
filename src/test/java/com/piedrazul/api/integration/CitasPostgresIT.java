package com.piedrazul.api.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.jayway.jsonpath.JsonPath;

/**
 * Prueba de integración de RF6 (cancelar cita) y del agendamiento: levanta la aplicación
 * completa con un servidor HTTP real y una base PostgreSQL real (perfil {@code it}).
 *
 * <p>Cubre lo que las pruebas unitarias con mocks no pueden demostrar: el índice único
 * parcial en PostgreSQL, los interceptores de autorización y el flujo completo
 * controlador → servicio → base de datos. Se ejecuta con {@code mvn verify}.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
class CitasPostgresIT {

    private static final String MEDICO = "doc-1";
    private static final String PACIENTE_EMAIL = "paciente@test.com";
    private static final String PACIENTE_CLAVE = "123456";
    private static final String ADMIN_EMAIL = "admin@piedrazul.com";
    private static final String ADMIN_CLAVE = "admin123";

    private record Respuesta(int status, String cuerpo) {
        String campo(String ruta) {
            return JsonPath.read(cuerpo, ruta).toString();
        }
    }

    @Value("${local.server.port}")
    private int puerto;

    @Autowired
    private JdbcTemplate jdbc;

    private final HttpClient http = HttpClient.newHttpClient();

    /** Día hábil dentro de la ventana de agendamiento, con anticipación de sobra para cancelar. */
    private static LocalDate fechaLejana;

    @BeforeAll
    static void calcularFecha() {
        LocalDate fecha = LocalDate.now().plusDays(5);
        while (fecha.getDayOfWeek() == DayOfWeek.SATURDAY || fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            fecha = fecha.plusDays(1);
        }
        fechaLejana = fecha;
    }

    // ---------------------------------------------------------------- utilidades

    private Respuesta enviar(String metodo, String ruta, String token, String json) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .header("Content-Type", "application/json");
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        req.method(metodo, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
        HttpResponse<String> r = http.send(req.build(), HttpResponse.BodyHandlers.ofString());
        return new Respuesta(r.statusCode(), r.body());
    }

    private String login(String email, String clave) throws Exception {
        Respuesta r = enviar("POST", "/api/auth/login", null,
                "{\"email\":\"" + email + "\",\"password\":\"" + clave + "\"}");
        assertThat(r.status()).as("login de " + email).isEqualTo(200);
        return r.campo("$.token");
    }

    private String tokenPaciente() throws Exception {
        return login(PACIENTE_EMAIL, PACIENTE_CLAVE);
    }

    private Respuesta agendar(String token, LocalDate fecha, String hora) throws Exception {
        return enviar("POST", "/api/citas/agendar", token,
                "{\"medicoId\":\"" + MEDICO + "\",\"fecha\":\"" + fecha + "\",\"hora\":\"" + hora + "\"}");
    }

    private Respuesta cancelar(String token, Object citaId, String motivo) throws Exception {
        return enviar("PUT", "/api/citas/" + citaId + "/cancelar", token, "{\"motivo\":\"" + motivo + "\"}");
    }

    private String agendarOk(String token, LocalDate fecha, String hora) throws Exception {
        Respuesta r = agendar(token, fecha, hora);
        assertThat(r.status()).as("agendar " + fecha + " " + hora).isEqualTo(201);
        return r.campo("$.id");
    }

    // ---------------------------------------------------------------- aplicación

    @Test
    void healthCheck_respondeUp() throws Exception {
        Respuesta r = enviar("GET", "/actuator/health", null, null);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.campo("$.status")).isEqualTo("UP");
    }

    // ---------------------------------------------------------------- base de datos

    @Test
    void postgres_tieneElIndiceUnicoParcialDeFranjaActiva() {
        List<String> definiciones = jdbc.queryForList(
                "SELECT indexdef FROM pg_indexes WHERE tablename = 'citas' AND indexname = 'uk_cita_franja_activa'",
                String.class);

        assertThat(definiciones).hasSize(1);
        assertThat(definiciones.get(0)).contains("UNIQUE").contains("CANCELADA");
    }

    @Test
    void postgres_rechazaDosCitasVigentesEnLaMismaFranja_peroPermiteReusarUnaCancelada() {
        String fecha = fechaLejana.plusDays(14).toString();
        String insertar = "INSERT INTO citas (paciente_id, medico_id, fecha, hora, estado) "
                + "VALUES (?, ?, CAST(? AS date), CAST('16:00' AS time), ?)";

        jdbc.update(insertar, "p1", MEDICO, fecha, "PENDIENTE");

        assertThatThrownBy(() -> jdbc.update(insertar, "p2", MEDICO, fecha, "CONFIRMADA"))
                .isInstanceOf(DataIntegrityViolationException.class);

        // Una cancelada no ocupa la franja, y puede haber varias
        jdbc.update(insertar, "p3", MEDICO, fecha, "CANCELADA");
        jdbc.update(insertar, "p4", MEDICO, fecha, "CANCELADA");
    }

    // ---------------------------------------------------------------- agendar y cancelar (RF6)

    @Test
    void cancelarConJustificacion_cambiaElEstadoYGuardaMotivoYFecha() throws Exception {
        String token = tokenPaciente();
        String id = agendarOk(token, fechaLejana, "08:00");

        Respuesta r = cancelar(token, id, "Viaje de trabajo");

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.campo("$.estado")).isEqualTo("CANCELADA");
        assertThat(r.campo("$.motivoCancelacion")).isEqualTo("Viaje de trabajo");

        // Lo persistido en PostgreSQL, no solo la respuesta
        assertThat(jdbc.queryForObject("SELECT estado FROM citas WHERE id = ?", String.class, Long.valueOf(id)))
                .isEqualTo("CANCELADA");
        assertThat(jdbc.queryForObject("SELECT motivo_cancelacion FROM citas WHERE id = ?", String.class, Long.valueOf(id)))
                .isEqualTo("Viaje de trabajo");
        assertThat(jdbc.queryForObject("SELECT fecha_cancelacion FROM citas WHERE id = ?", Object.class, Long.valueOf(id)))
                .isNotNull();
    }

    @Test
    void franjaVigente_noSePuedeReservarDosVeces() throws Exception {
        String token = tokenPaciente();
        agendarOk(token, fechaLejana, "08:30");

        Respuesta repetida = agendar(token, fechaLejana, "08:30");

        assertThat(repetida.status()).isEqualTo(409);
    }

    @Test
    void franjaDeUnaCitaCancelada_sePuedeReservarDeNuevo() throws Exception {
        String token = tokenPaciente();
        String id = agendarOk(token, fechaLejana, "09:00");
        assertThat(cancelar(token, id, "Cambio de planes").status()).isEqualTo(200);

        Respuesta nueva = agendar(token, fechaLejana, "09:00");

        assertThat(nueva.status()).isEqualTo(201);
        assertThat(nueva.campo("$.id")).isNotEqualTo(id);
    }

    @Test
    void cancelarSinJustificacion_devuelve400() throws Exception {
        String token = tokenPaciente();
        String id = agendarOk(token, fechaLejana, "09:30");

        assertThat(cancelar(token, id, "").status()).isEqualTo(400);
        assertThat(jdbc.queryForObject("SELECT estado FROM citas WHERE id = ?", String.class, Long.valueOf(id)))
                .as("la cita no debe cambiar").isEqualTo("PENDIENTE");
    }

    @Test
    void cancelarUnaCitaYaCancelada_devuelve400() throws Exception {
        String token = tokenPaciente();
        String id = agendarOk(token, fechaLejana, "10:00");
        assertThat(cancelar(token, id, "Primera vez").status()).isEqualTo(200);

        assertThat(cancelar(token, id, "Segunda vez").status()).isEqualTo(400);
    }

    @Test
    void cancelarConMenosDeDosDiasDeAnticipacion_devuelve400() throws Exception {
        LocalDate manana = LocalDate.now().plusDays(1);
        assumeTrue(manana.getDayOfWeek() != DayOfWeek.SATURDAY && manana.getDayOfWeek() != DayOfWeek.SUNDAY,
                "mañana no es día hábil: no se puede agendar para probar la regla");
        String token = tokenPaciente();
        String id = agendarOk(token, manana, "11:00");

        Respuesta r = cancelar(token, id, "Ya no puedo asistir");

        assertThat(r.status()).isEqualTo(400);
        assertThat(r.campo("$.message")).contains("2 días");
    }

    // ---------------------------------------------------------------- autorización

    @Test
    void cancelarLaCitaDeOtroPaciente_devuelve403() throws Exception {
        String dueno = tokenPaciente();
        String id = agendarOk(dueno, fechaLejana, "10:30");

        String email = "otro-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        Respuesta registro = enviar("POST", "/api/auth/register", null,
                "{\"nombreCompleto\":\"Otro Paciente\",\"email\":\"" + email + "\","
                        + "\"numeroIdentificacion\":\"" + System.nanoTime() + "\",\"numeroTelefonico\":\"3000000000\","
                        + "\"password\":\"123456\",\"confirmPassword\":\"123456\"}");
        assertThat(registro.status()).isEqualTo(201);

        assertThat(cancelar(login(email, "123456"), id, "No es mi cita").status()).isEqualTo(403);
    }

    @Test
    void soloUnPacientePuedeCancelar_elAdminRecibe403() throws Exception {
        String id = agendarOk(tokenPaciente(), fechaLejana, "11:30");

        assertThat(cancelar(login(ADMIN_EMAIL, ADMIN_CLAVE), id, "x").status()).isEqualTo(403);
    }

    @Test
    void cancelarSinToken_devuelve401() throws Exception {
        assertThat(cancelar(null, 1, "x").status()).isEqualTo(401);
    }

    @Test
    void cancelarUnaCitaQueNoExiste_devuelve404() throws Exception {
        assertThat(cancelar(tokenPaciente(), 987654321L, "x").status()).isEqualTo(404);
    }
}
