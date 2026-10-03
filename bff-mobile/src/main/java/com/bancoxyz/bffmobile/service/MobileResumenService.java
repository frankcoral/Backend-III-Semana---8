package com.bancoxyz.bffmobile.service;

import com.bancoxyz.bffmobile.client.CoreBancoClient;
import com.bancoxyz.bffmobile.dto.MobileMovimientoDto;
import com.bancoxyz.bffmobile.dto.MobileSummaryDto;
import com.bancoxyz.bffmobile.dto.MovimientoAnualDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MobileResumenService {

        private static final Logger log = LoggerFactory.getLogger(MobileResumenService.class);

        private final CoreBancoClient coreClient;
        private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

        public MobileResumenService(
                        CoreBancoClient coreClient,
                        CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
                this.coreClient = coreClient;
                this.circuitBreakerFactory = circuitBreakerFactory;
        }

        public MobileSummaryDto resumen() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
                        throw new IllegalStateException(
                                        "No existe un JWT OAuth2 válido en el contexto de seguridad");
                }

                /*
                 * Semana 8:
                 * El JWT se captura en el hilo HTTP antes de entrar al Circuit Breaker.
                 * Resilience4j puede ejecutar el Supplier en otro hilo, por lo que el
                 * token se pasa explícitamente al CoreBancoClient.
                 */
                String accessToken = jwtAuthentication.getToken().getTokenValue();

                return circuitBreakerFactory.create("coreBancoMobile").run(
                                () -> resumenCore(accessToken),
                                this::fallbackResumen);
        }

        private MobileSummaryDto resumenCore(String accessToken) {
                List<MobileMovimientoDto> ultimos = coreClient.movimientos(5, accessToken).stream()
                                .map(this::ligero)
                                .toList();

                return new MobileSummaryDto(
                                "MOBILE",
                                "BFF Mobile independiente: payload reducido con datos esenciales",
                                coreClient.resumenTransacciones(accessToken),
                                coreClient.resumenEstadosAnuales(accessToken),
                                ultimos);
        }

        private MobileSummaryDto fallbackResumen(Throwable throwable) {
                log.error(
                                "BFF_MOBILE_FALLBACK -> tipo={}, mensaje={}",
                                throwable.getClass().getName(),
                                throwable.getMessage(),
                                throwable);

                return new MobileSummaryDto(
                                "MOBILE",
                                "FALLBACK ACTIVO - CORE Banco XYZ no disponible; respuesta degradada entregada por Resilience4j",
                                null,
                                null,
                                List.of());
        }

        private MobileMovimientoDto ligero(MovimientoAnualDto movimiento) {
                return new MobileMovimientoDto(
                                movimiento.cuentaId(),
                                movimiento.transaccion(),
                                movimiento.monto());
        }
}
