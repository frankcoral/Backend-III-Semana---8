package com.bancoxyz.core.event;

import java.math.BigDecimal;
import java.time.Instant;

public record RetiroResultadoEvent(
        String eventId,
        String transaccionId,
        String tipoEvento,
        Integer cuentaId,
        BigDecimal monto,
        BigDecimal saldoAnterior,
        BigDecimal saldoFinal,
        String estado,
        String motivo,
        Instant fechaHora) {
}