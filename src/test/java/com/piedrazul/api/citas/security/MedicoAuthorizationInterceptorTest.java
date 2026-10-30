package com.piedrazul.api.citas.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.provider.TokenProvider;

@ExtendWith(MockitoExtension.class)
class MedicoAuthorizationInterceptorTest {

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private MedicoAuthorizationInterceptor interceptor;

    @Test
    void preHandle_conTokenMedico_permiteAcceso() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer medico-token");
        when(tokenProvider.validateToken("medico-token")).thenReturn(true);
        when(tokenProvider.getRoleFromToken("medico-token")).thenReturn(RoleEnum.MEDICO);

        boolean allowed = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertThat(allowed).isTrue();
    }

    @Test
    void preHandle_conTokenTerapista_deniegaAcceso() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer terapista-token");
        when(tokenProvider.validateToken("terapista-token")).thenReturn(true);
        when(tokenProvider.getRoleFromToken("terapista-token")).thenReturn(RoleEnum.THERAPIST);

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void preHandle_sinToken_retornaNoAutorizado() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void preHandle_conTokenInvalido_retornaNoAutorizado() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-vencido");
        when(tokenProvider.validateToken("token-vencido")).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void preHandle_preflightCors_permiteAccesoSinToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/medico/perfil");

        boolean allowed = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertThat(allowed).isTrue();
    }
}