package com.bancoxyz.core.service;

import com.bancoxyz.core.event.RetiroResultadoEvent;
import com.bancoxyz.core.event.RetiroSolicitadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RetiroCoreService {

    private static final Logger log = LoggerFactory.getLogger(RetiroCoreService.class);

    private final LegacyDataService legacyDataService;

    /*
     * Estado operativo en memoria para la actividad. Core es el unico componente
     * que modifica el saldo; el BFF ATM solo publica la solicitud.
     */
    private final Map<Integer, BigDecimal> saldosOperativos = new ConcurrentHashMap<>();

    /*
     * Store de eventos ya procesados. Permite una semantica at-least-once sin
     * volver a aplicar el retiro cuando Kafka reentrega el mismo eventId.
     */
    private final Map<String, RetiroResultadoEvent> eventosProcesados = new ConcurrentHashMap<>();

    /*
     * El bloqueo es por cuenta y no global: cuentas distintas pueden procesarse
     * en paralelo, mientras una misma cuenta conserva consistencia y orden.
     */
    private final Map<Integer, Object> bloqueosCuenta = new ConcurrentHashMap<>();

    public RetiroCoreService(LegacyDataService legacyDataService) {
        this.legacyDataService = legacyDataService;
    }

    public RetiroResultadoEvent procesar(RetiroSolicitadoEvent event) {
        if (event == null || event.eventId() == null || event.eventId().isBlank()) {
            throw new IllegalArgumentException("El evento de retiro debe incluir eventId");
        }
        if (event.transaccionId() == null || event.transaccionId().isBlank()) {
            throw new IllegalArgumentException("El evento de retiro debe incluir transaccionId");
        }
        if (event.cuentaId() == null) {
            throw new IllegalArgumentException("El evento de retiro debe incluir cuentaId");
        }

        RetiroResultadoEvent resultadoAnterior = eventosProcesados.get(event.eventId());
        if (resultadoAnterior != null) {
            log.info(
                    "[IDEMPOTENCIA] Evento ya procesado eventId={} transaccionId={} estado={}",
                    event.eventId(),
                    event.transaccionId(),
                    resultadoAnterior.estado());
            return resultadoAnterior;
        }

        Object bloqueoCuenta = bloqueosCuenta.computeIfAbsent(event.cuentaId(), ignored -> new Object());

        synchronized (bloqueoCuenta) {
            /* Revalidacion dentro del lock por seguridad ante concurrencia. */
            resultadoAnterior = eventosProcesados.get(event.eventId());
            if (resultadoAnterior != null) {
                log.info(
                        "[IDEMPOTENCIA] Evento ya procesado dentro del lock eventId={} transaccionId={}",
                        event.eventId(),
                        event.transaccionId());
                return resultadoAnterior;
            }

            BigDecimal saldoAnterior = saldosOperativos.computeIfAbsent(
                    event.cuentaId(),
                    legacyDataService::saldoOperativoInicial);

            RetiroResultadoEvent resultado;

            if (saldoAnterior == null) {
                resultado = resultadoRechazado(event, null, "CUENTA_SIN_SALDO_VALIDO");
            } else if (event.monto() == null || event.monto().signum() <= 0) {
                resultado = resultadoRechazado(event, saldoAnterior, "MONTO_INVALIDO");
            } else if (event.monto().compareTo(saldoAnterior) > 0) {
                resultado = resultadoRechazado(event, saldoAnterior, "SALDO_INSUFICIENTE");
            } else {
                BigDecimal saldoFinal = saldoAnterior.subtract(event.monto());
                saldosOperativos.put(event.cuentaId(), saldoFinal);

                resultado = new RetiroResultadoEvent(
                        UUID.randomUUID().toString(),
                        event.transaccionId(),
                        "RETIRO_APROBADO",
                        event.cuentaId(),
                        event.monto(),
                        saldoAnterior,
                        saldoFinal,
                        "APROBADO",
                        "RETIRO_PROCESADO",
                        Instant.now());
            }

            eventosProcesados.put(event.eventId(), resultado);
            return resultado;
        }
    }

    private RetiroResultadoEvent resultadoRechazado(
            RetiroSolicitadoEvent event,
            BigDecimal saldoAnterior,
            String motivo) {

        return new RetiroResultadoEvent(
                UUID.randomUUID().toString(),
                event.transaccionId(),
                "RETIRO_RECHAZADO",
                event.cuentaId(),
                event.monto(),
                saldoAnterior,
                saldoAnterior,
                "RECHAZADO",
                motivo,
                Instant.now());
    }
}
