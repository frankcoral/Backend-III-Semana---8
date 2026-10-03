package com.bancoxyz.core.service;

import com.bancoxyz.core.event.RetiroResultadoEvent;
import com.bancoxyz.core.event.RetiroSolicitadoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RetiroCoreServiceTest {

    private RetiroCoreService service;

    @BeforeEach
    void setUp() {
        service = new RetiroCoreService(new LegacyDataService());
    }

    @Test
    void apruebaRetiroYActualizaSaldo() {
        RetiroResultadoEvent resultado = service.procesar(evento("evt-1", "tx-1", "1000"));

        assertEquals("APROBADO", resultado.estado());
        assertEquals("RETIRO_PROCESADO", resultado.motivo());
        assertEquals(new BigDecimal("8000"), resultado.saldoAnterior());
        assertEquals(new BigDecimal("7000"), resultado.saldoFinal());
    }

    @Test
    void rechazaRetiroPorSaldoInsuficiente() {
        RetiroResultadoEvent resultado = service.procesar(evento("evt-2", "tx-2", "15000"));

        assertEquals("RECHAZADO", resultado.estado());
        assertEquals("SALDO_INSUFICIENTE", resultado.motivo());
        assertEquals(new BigDecimal("8000"), resultado.saldoAnterior());
        assertEquals(new BigDecimal("8000"), resultado.saldoFinal());
    }

    @Test
    void mismoEventIdNoDescuentaDosVeces() {
        RetiroSolicitadoEvent eventoDuplicado = evento("evt-idempotente", "tx-idempotente", "1000");

        RetiroResultadoEvent primeraEntrega = service.procesar(eventoDuplicado);
        RetiroResultadoEvent segundaEntrega = service.procesar(eventoDuplicado);
        RetiroResultadoEvent siguienteRetiro = service.procesar(evento("evt-3", "tx-3", "1000"));

        assertEquals(primeraEntrega, segundaEntrega);
        assertEquals(new BigDecimal("7000"), primeraEntrega.saldoFinal());
        assertEquals(new BigDecimal("7000"), siguienteRetiro.saldoAnterior());
        assertEquals(new BigDecimal("6000"), siguienteRetiro.saldoFinal());
    }

    private RetiroSolicitadoEvent evento(String eventId, String transaccionId, String monto) {
        return new RetiroSolicitadoEvent(
                eventId,
                transaccionId,
                "RETIRO_SOLICITADO",
                101,
                new BigDecimal(monto),
                "ATM",
                Instant.parse("2026-09-25T13:00:00Z"));
    }
}
