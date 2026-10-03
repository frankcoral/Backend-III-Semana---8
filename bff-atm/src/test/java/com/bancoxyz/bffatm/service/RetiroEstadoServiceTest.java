package com.bancoxyz.bffatm.service;

import com.bancoxyz.bffatm.dto.RetiroEstadoDto;
import com.bancoxyz.bffatm.event.RetiroResultadoEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetiroEstadoServiceTest {

    @Test
    void cambiaDePendienteAResultadoFinal() {
        RetiroEstadoService service = new RetiroEstadoService();

        service.registrarPendiente("tx-1", 101, new BigDecimal("1000"));
        RetiroEstadoDto pendiente = service.buscar("tx-1").orElseThrow();
        assertEquals("PENDIENTE", pendiente.estado());

        service.registrarResultado(new RetiroResultadoEvent(
                "resultado-1",
                "tx-1",
                "RETIRO_APROBADO",
                101,
                new BigDecimal("1000"),
                new BigDecimal("8000"),
                new BigDecimal("7000"),
                "APROBADO",
                "RETIRO_PROCESADO",
                Instant.parse("2026-09-25T13:01:00Z")));

        RetiroEstadoDto finalizado = service.buscar("tx-1").orElseThrow();
        assertEquals("APROBADO", finalizado.estado());
        assertEquals(new BigDecimal("7000"), finalizado.saldoFinal());
        assertTrue(service.buscar("tx-inexistente").isEmpty());
    }
}
