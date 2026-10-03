package com.bancoxyz.bffweb.dto;

import java.math.BigDecimal;

public record ResumenTransaccionesDto(
        Integer registros,
        Long creditos,
        Long debitos,
        Long otrosTipos,
        Long montosAusentes,
        BigDecimal montoTotalInformado) {}
