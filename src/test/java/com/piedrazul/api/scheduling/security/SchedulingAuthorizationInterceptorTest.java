package com.piedrazul.api.scheduling.security;

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
class SchedulingAuthorizationInterceptorTest {

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private SchedulingAuthorizationInterceptor interceptor;

    @Test
    void preHandle_conTokenAdmin_permiteAcceso() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer admin-token");
        when(tokenProvider.validateToken("admin-token")).thenReturn(true);
        when(tokenProvider.getRoleFromToken("admin-token")).thenReturn(RoleEnum.ADMIN);

        boolean allowed = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertThat(allowed).isTrue();
    }

    @Test
    void preHandle_conRolNoAdmin_deniegaAcceso() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer patient-token");
        when(tokenProvider.validateToken("patient-token")).thenReturn(true);
        when(tokenProvider.getRoleFromToken("patient-token")).thenReturn(RoleEnum.PATIENT);

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
    void preHandle_preflightCors_permiteAccesoSinToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/admin/doctors");

        boolean allowed = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertThat(allowed).isTrue();
    }
}