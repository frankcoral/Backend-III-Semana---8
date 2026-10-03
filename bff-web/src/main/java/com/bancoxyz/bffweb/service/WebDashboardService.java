package com.bancoxyz.bffweb.service;

import com.bancoxyz.bffweb.client.CoreBancoClient;
import com.bancoxyz.bffweb.dto.WebDashboardDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebDashboardService {

        private static final Logger log = LoggerFactory.getLogger(WebDashboardService.class);

        private final CoreBancoClient coreClient;
        private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

        public WebDashboardService(
                        CoreBancoClient coreClient,
                        CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
                this.coreClient = coreClient;
                this.circuitBreakerFactory = circuitBreakerFactory;
        }

        public WebDashboardDto dashboard() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
                        throw new IllegalStateException(
                                        "No existe un JWT OAuth2 válido en el contexto de seguridad");
                }

                /*
                 * Importante:
                 * capturamos el token en el hilo HTTP antes de entrar al Circuit Breaker.
                 * Resilience4j puede ejecutar el Supplier en otro hilo, por lo que no
                 * debemos volver a depender de SecurityContextHolder dentro de ese hilo.
                 */
                String accessToken = jwtAuthentication.getToken().getTokenValue();

                return circuitBreakerFactory.create("coreBancoWeb").run(
                                () -> dashboardCore(accessToken),
                                this::fallbackDashboard);
        }

        private WebDashboardDto dashboardCore(String accessToken) {
                return new WebDashboardDto(
                                "WEB",
                                "BFF Web independiente: respuesta completa para navegadores e interfaces complejas",
                                coreClient.resumen(accessToken),
                                coreClient.transacciones(10, accessToken),
                                coreClient.intereses(10, accessToken),
                                coreClient.movimientos(10, accessToken));
        }

        private WebDashboardDto fallbackDashboard(Throwable throwable) {
                log.error(
                                "BFF_WEB_FALLBACK -> tipo={}, mensaje={}",
                                throwable.getClass().getName(),
                                throwable.getMessage(),
                                throwable);

                return new WebDashboardDto(
                                "WEB",
                                "FALLBACK ACTIVO - CORE Banco XYZ no disponible; respuesta degradada entregada por Resilience4j",
                                null,
                                List.of(),
                                List.of(),
                                List.of());
        }
}
