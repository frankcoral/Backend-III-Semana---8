package com.bancoxyz.bffatm.dto;

import java.math.BigDecimal;

public record SaldoInicialDto(
        Integer cuentaId,
        BigDecimal saldo) {}
