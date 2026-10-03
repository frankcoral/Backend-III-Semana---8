package com.bancoxyz.bffatm.dto;

import java.math.BigDecimal;

public record AtmWithdrawalDto(
        String canal,
        Integer cuentaId,
        BigDecimal retiro,
        BigDecimal saldoAnterior,
        BigDecimal saldoFinal,
        String estado) {}
