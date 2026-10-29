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

@Component
public class CitasAuthorizationInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final Set<RoleEnum> EXPORT_ROLES =
        Set.of(RoleEnum.THERAPIST, RoleEnum.ADMIN);

    private static final Set<RoleEnum> HISTORIAL_ROLES =
        Set.of(RoleEnum.THERAPIST, RoleEnum.ADMIN, RoleEnum.PATIENT);

    private final TokenProvider tokenProvider;

    public CitasAuthorizationInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

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

        if (role == null) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "El token no contiene un rol valido");
        }

        String path = request.getRequestURI();

        if (path.startsWith("/api/citas/exportar")) {
            if (!EXPORT_ROLES.contains(role)) {
                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Rol no autorizado para exportar citas");
            }
            return true;
        }

        if (path.startsWith("/api/citas/paciente/")) {
            if (!HISTORIAL_ROLES.contains(role)) {
                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Rol no autorizado para consultar historial");
            }
            return true;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ruta no autorizada");
    }
}