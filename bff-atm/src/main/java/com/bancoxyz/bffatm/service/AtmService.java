package com.bancoxyz.bffatm.service;

import com.bancoxyz.bffatm.client.CoreBancoClient;
import com.bancoxyz.bffatm.dto.AtmBalanceDto;
import com.bancoxyz.bffatm.dto.RetiroAceptadoDto;
import com.bancoxyz.bffatm.dto.SaldoInicialDto;
import com.bancoxyz.bffatm.error.CoreBancoUnavailableException;
import com.bancoxyz.bffatm.event.RetiroSolicitadoEvent;
import com.bancoxyz.bffatm.messaging.RetiroEventProducer;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class AtmService {

        private final CoreBancoClient coreClient;
        private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
        private final RetiroEventProducer retiroEventProducer;
        private final RetiroEstadoService retiroEstadoService;
        private final SaldoCuentaService saldoCuentaService;

        public AtmService(
                        CoreBancoClient coreClient,
                        CircuitBreakerFactory<?, ?> circuitBreakerFactory,
                        RetiroEventProducer retiroEventProducer,
                        RetiroEstadoService retiroEstadoService,
                        SaldoCuentaService saldoCuentaService) {
                this.coreClient = coreClient;
                this.circuitBreakerFactory = circuitBreakerFactory;
                this.retiroEventProducer = retiroEventProducer;
                this.retiroEstadoService = retiroEstadoService;
                this.saldoCuentaService = saldoCuentaService;
        }

        public AtmBalanceDto consultarSaldo(Integer cuentaId) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
                        throw new IllegalStateException(
                                        "No existe un JWT OAuth2 válido en el contexto de seguridad");
                }

                /*
                 * Semana 8:
                 * Capturamos el JWT antes de entrar al Circuit Breaker.
                 * Resilience4j puede ejecutar la llamada al Core en otro hilo.
                 */
                String accessToken = jwtAuthentication.getToken().getTokenValue();

                return new AtmBalanceDto(
                                "ATM",
                                cuentaId,
                                obtenerSaldo(cuentaId, accessToken));
        }

        public RetiroAceptadoDto solicitarRetiro(
                        Integer cuentaId,
                        BigDecimal monto) {

                String transaccionId = UUID.randomUUID().toString();
                String eventId = UUID.randomUUID().toString();

                RetiroSolicitadoEvent event = new RetiroSolicitadoEvent(
                                eventId,
                                transaccionId,
                                "RETIRO_SOLICITADO",
                                cuentaId,
                                monto,
                                "ATM",
                                Instant.now());

                retiroEstadoService.registrarPendiente(transaccionId, cuentaId, monto);
                retiroEventProducer.publicar(event);

                return new RetiroAceptadoDto(
                                "ATM",
                                cuentaId,
                                monto,
                                transaccionId,
                                "PENDIENTE",
                                "Retiro recibido y enviado para procesamiento asincrono");
        }

        private BigDecimal obtenerSaldo(Integer cuentaId, String accessToken) {
                return saldoCuentaService.obtenerOCargar(
                                cuentaId,
                                () -> cargarSaldoConResilience(cuentaId, accessToken));
        }

        private BigDecimal cargarSaldoConResilience(
                        Integer cuentaId,
                        String accessToken) {

                SaldoInicialDto data = circuitBreakerFactory
                                .create("coreBancoAtm")
                                .run(
                                                () -> coreClient.saldoInicial(cuentaId, accessToken),
                                                throwable -> {
                                                        throw new CoreBancoUnavailableException(
                                                                        "FALLBACK ACTIVO - CORE Banco XYZ no disponible; ATM evita informar un saldo no verificado");
                                                });

                if (data == null || data.saldo() == null) {
                        throw new IllegalArgumentException(
                                        "No existe saldo para la cuenta " + cuentaId);
                }

                return data.saldo();
        }
}
