package com.bancoxyz.bffatm.dto;

import java.math.BigDecimal;

public record RetiroAceptadoDto(
        String canal,
        Integer cuentaId,
        BigDecimal monto,
        String transaccionId,
        String estado,
        String mensaje) {
}