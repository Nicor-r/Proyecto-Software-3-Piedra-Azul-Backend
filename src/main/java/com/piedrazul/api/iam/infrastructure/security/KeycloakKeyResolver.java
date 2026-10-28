package com.piedrazul.api.iam.infrastructure.security;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Key;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.JwkSet;
import io.jsonwebtoken.security.Jwks;

/**
 * Resuelve y almacena en caché las llaves públicas RSA de Keycloak
 * obtenidas desde el endpoint JWKS (certs).
 */
@Component
public class KeycloakKeyResolver {

    private static final Logger log = LoggerFactory.getLogger(KeycloakKeyResolver.class);

    private final String jwksUrl;
    private final HttpClient httpClient;
    private final Map<String, Key> keyCache = new ConcurrentHashMap<>();

    @Autowired
    public KeycloakKeyResolver(
            @Value("${app.keycloak.jwks-url:http://localhost:8180/realms/piedrazul/protocol/openid-connect/certs}") String jwksUrl) {
        this(jwksUrl, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
    }

    public KeycloakKeyResolver(String jwksUrl, HttpClient httpClient) {
        this.jwksUrl = jwksUrl;
        this.httpClient = httpClient;
    }

    /**
     * Resuelve la llave pública asociada al Key ID (kid) especificado en el encabezado del JWT.
     * Si no se encuentra en caché, refresca el JWKS desde el servidor de Keycloak.
     */
    public Key resolveKey(String kid) {
        if (kid != null && keyCache.containsKey(kid)) {
            return keyCache.get(kid);
        }

        refreshKeys();

        if (kid != null) {
            return keyCache.get(kid);
        }

        // Si no vino kid en el token, retorna la primera llave disponible si existe
        return keyCache.values().stream().findFirst().orElse(null);
    }

    /**
     * Permite registrar manualmente una llave en caché (útil para pruebas unitarias sin HTTP).
     */
    public void registerKey(String kid, Key key) {
        this.keyCache.put(kid, key);
    }

    private synchronized void refreshKeys() {
        if (jwksUrl == null || jwksUrl.isBlank()) {
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(jwksUrl))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JwkSet jwkSet = Jwks.setParser().build().parse(response.body());
                for (Jwk<?> jwk : jwkSet.getKeys()) {
                    if (jwk.getId() != null) {
                        keyCache.put(jwk.getId(), jwk.toKey());
                    }
                }
                log.debug("Se cargaron {} llaves públicas desde Keycloak JWKS", jwkSet.getKeys().size());
            } else {
                log.warn("Respuesta no exitosa al obtener JWKS de Keycloak (HTTP {}): {}",
                        response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("No fue posible contactar a Keycloak en {}: {}", jwksUrl, e.getMessage());
        }
    }
}
