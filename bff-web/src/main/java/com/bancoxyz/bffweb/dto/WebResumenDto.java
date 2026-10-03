package com.bancoxyz.bffweb.dto;

public record WebResumenDto(
        ResumenTransaccionesDto transacciones,
        ResumenInteresesDto intereses,
        ResumenEstadosAnualesDto estadosAnuales) {}
