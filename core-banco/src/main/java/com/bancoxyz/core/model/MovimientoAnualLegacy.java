package com.bancoxyz.core.model;

import java.math.BigDecimal;

public record MovimientoAnualLegacy(
        Integer cuentaId,
        String fecha,
        String transaccion,
        BigDecimal monto,
        String descripcion) {
}
