package com.bancoxyz.bffmobile.dto;

import java.util.List;

public record MobileSummaryDto(
        String canal,
        String descripcion,
        ResumenTransaccionesDto transacciones,
        ResumenEstadosAnualesDto estadosAnuales,
        List<MobileMovimientoDto> ultimosMovimientos) {}
