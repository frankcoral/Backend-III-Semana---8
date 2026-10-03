package com.bancoxyz.core.model;

import java.math.BigDecimal;

public record TransaccionLegacy(
        Integer id,
        String fecha,
        BigDecimal monto,
        String tipo) {
}
