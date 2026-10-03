package com.bancoxyz.bffatm.service;

import com.bancoxyz.bffatm.dto.RetiroEstadoDto;
import com.bancoxyz.bffatm.event.RetiroResultadoEvent;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RetiroEstadoService {

    private final Map<String, RetiroEstadoDto> estados = new ConcurrentHashMap<>();

    public void registrarPendiente(
            String transaccionId,
            Integer cuentaId,
            BigDecimal monto) {

        estados.put(
                transaccionId,
                new RetiroEstadoDto(
                        transaccionId,
                        cuentaId,
                        monto,
                        "PENDIENTE",
                        "ENVIADO_A_KAFKA",
                        null,
                        null,
                        Instant.now()));
    }

    public void registrarResultado(RetiroResultadoEvent event) {
        estados.put(
                event.transaccionId(),
                new RetiroEstadoDto(
                        event.transaccionId(),
                        event.cuentaId(),
                        event.monto(),
                        event.estado(),
                        event.motivo(),
                        event.saldoAnterior(),
                        event.saldoFinal(),
                        event.fechaHora()));
    }

    public void registrarErrorPublicacion(
            String transaccionId,
            Integer cuentaId,
            BigDecimal monto) {

        estados.put(
                transaccionId,
                new RetiroEstadoDto(
                        transaccionId,
                        cuentaId,
                        monto,
                        "ERROR_PUBLICACION",
                        "KAFKA_NO_DISPONIBLE",
                        null,
                        null,
                        Instant.now()));
    }

    public Optional<RetiroEstadoDto> buscar(String transaccionId) {
        return Optional.ofNullable(estados.get(transaccionId));
    }
}
