package com.piedrazul.api.citas.security;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.piedrazul.api.citas.exception.UnauthorizedException;
import com.piedrazul.api.iam.provider.TokenProvider;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class AuthenticatedUserArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    public AuthenticatedUserArgumentResolver(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticatedUser.class)
                && parameter.getParameterType().equals(String.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new UnauthorizedException("No se pudo obtener la petición HTTP");
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException("Falta el token de autenticación");
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (!tokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Token inválido o expirado");
        }

        return tokenProvider.getEmailFromToken(token);
    }
}