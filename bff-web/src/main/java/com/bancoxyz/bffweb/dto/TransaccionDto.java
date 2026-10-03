package com.bancoxyz.bffweb.dto;
import java.math.BigDecimal;
public record TransaccionDto(Integer id, String fecha, BigDecimal monto, String tipo) {}
