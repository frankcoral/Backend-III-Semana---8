package com.bancoxyz.bffweb.dto;

import java.util.List;

public record WebDashboardDto(
        String canal,
        String descripcion,
        WebResumenDto resumen,
        List<TransaccionDto> muestraTransacciones,
        List<CuentaInteresDto> muestraIntereses,
        List<MovimientoAnualDto> muestraEstadosAnuales) {}
