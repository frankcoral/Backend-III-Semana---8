package com.bancoxyz.bffweb.dto;
import java.math.BigDecimal;
public record CuentaInteresDto(Integer cuentaId, String nombre, BigDecimal saldo, Integer edad, String tipo) {}
