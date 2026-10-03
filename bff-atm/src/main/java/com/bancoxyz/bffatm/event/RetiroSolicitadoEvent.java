package com.bancoxyz.bffatm.event;

import java.math.BigDecimal;
import java.time.Instant;

public record RetiroSolicitadoEvent(
        String eventId,
        String transaccionId,
        String tipoEvento,
        Integer cuentaId,
        BigDecimal monto,
        String canal,
        Instant fechaHora) {
}