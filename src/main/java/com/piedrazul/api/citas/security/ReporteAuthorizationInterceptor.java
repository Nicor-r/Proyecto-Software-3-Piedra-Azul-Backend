package com.piedrazul.api.citas.security;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.provider.TokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor de autorizacion para los endpoints de reportes (/api/reportes/**).
 * Permite el acceso unicamente a tokens validos con rol MEDICO, THERAPIST o ADMIN.
 */
@Component
public class ReporteAuthorizationInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final Set<RoleEnum> REPORT_ROLES =
            Set.of(RoleEnum.MEDICO, RoleEnum.THERAPIST, RoleEnum.ADMIN);

    private final TokenProvider tokenProvider;

    public ReporteAuthorizationInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Falta el token de autenticacion");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (!tokenProvider.validateToken(token)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Token invalido o expirado");
        }

        RoleEnum role;
        try {
            role = tokenProvider.getRoleFromToken(token);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Token con rol invalido");
        }

        if (role == null || !REPORT_ROLES.contains(role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Rol no autorizado para consultar reportes");
        }

        return true;
    }
}
