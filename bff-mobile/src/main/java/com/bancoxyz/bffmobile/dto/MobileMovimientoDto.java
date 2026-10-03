package com.bancoxyz.bffmobile.dto;

import java.math.BigDecimal;

public record MobileMovimientoDto(
        Integer cuentaId,
        String tipo,
        BigDecimal monto) {}
