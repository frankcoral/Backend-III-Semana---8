package com.bancoxyz.bffweb.dto;

public record ResumenInteresesDto(
        Integer registros,
        Long cuentasDistintas,
        Long ahorro,
        Long prestamo,
        Long hipoteca,
        Long otrosTipos,
        Long saldosAusentes,
        Long edadesAusentes) {}
