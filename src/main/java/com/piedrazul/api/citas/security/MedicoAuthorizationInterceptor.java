package com.piedrazul.api.citas.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.provider.TokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Portero de las rutas exclusivas del actor MEDICO (/api/medico/**).
 * Solo deja pasar tokens validos cuyo rol sea MEDICO (HE-04 y HE-05).
 */
@Component
public class MedicoAuthorizationInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    public MedicoAuthorizationInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // El navegador envia una peticion OPTIONS (preflight CORS) sin token antes de la real.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. ¿Trae carne?
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falta el token de autenticacion");
        }

        // 2. ¿Es valido y no esta vencido?
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (!tokenProvider.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido o expirado");
        }

        // 3. ¿El gafete dice MEDICO?
        RoleEnum role = tokenProvider.getRoleFromToken(token);
        if (role != RoleEnum.MEDICO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere el rol MEDICO");
        }

        return true;
    }
}