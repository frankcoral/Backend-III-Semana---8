package com.bancoxyz.bffmobile.dto;

public record ResumenEstadosAnualesDto(
        Integer registros,
        Long cuentasDistintas,
        Long depositos,
        Long retiros,
        Long compras,
        Long otrosTipos,
        Long montosAusentes) {}
