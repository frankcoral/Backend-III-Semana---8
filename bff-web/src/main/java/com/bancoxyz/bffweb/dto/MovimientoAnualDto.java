package com.bancoxyz.bffweb.dto;
import java.math.BigDecimal;
public record MovimientoAnualDto(Integer cuentaId, String fecha, String transaccion, BigDecimal monto, String descripcion) {}
