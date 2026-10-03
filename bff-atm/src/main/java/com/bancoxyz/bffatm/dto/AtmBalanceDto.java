package com.bancoxyz.bffatm.dto;

import java.math.BigDecimal;

public record AtmBalanceDto(
        String canal,
        Integer cuentaId,
        BigDecimal saldo) {}
