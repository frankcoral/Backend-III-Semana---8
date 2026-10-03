package com.bancoxyz.bffweb.dto;

public record ResumenEstadosAnualesDto(
        Integer registros,
        Long cuentasDistintas,
        Long depositos,
        Long retiros,
        Long compras,
        Long otrosTipos,
        Long montosAusentes) {}
