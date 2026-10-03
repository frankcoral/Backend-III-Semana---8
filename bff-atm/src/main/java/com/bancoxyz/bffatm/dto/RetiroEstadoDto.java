package com.bancoxyz.bffatm.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RetiroEstadoDto(
        String transaccionId,
        Integer cuentaId,
        BigDecimal monto,
        String estado,
        String motivo,
        BigDecimal saldoAnterior,
        BigDecimal saldoFinal,
        Instant actualizadoEn) {
}
