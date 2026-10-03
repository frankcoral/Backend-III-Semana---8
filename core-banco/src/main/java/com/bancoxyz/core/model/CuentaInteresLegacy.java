package com.bancoxyz.core.model;

import java.math.BigDecimal;

public record CuentaInteresLegacy(
        Integer cuentaId,
        String nombre,
        BigDecimal saldo,
        Integer edad,
        String tipo) {
}
