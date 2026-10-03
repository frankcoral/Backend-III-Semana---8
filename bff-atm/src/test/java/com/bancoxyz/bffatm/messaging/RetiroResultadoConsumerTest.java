package com.bancoxyz.bffatm.messaging;

import com.bancoxyz.bffatm.event.RetiroResultadoEvent;
import com.bancoxyz.bffatm.service.RetiroEstadoService;
import com.bancoxyz.bffatm.service.SaldoCuentaService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RetiroResultadoConsumerTest {

    @Test
    void resultadoKafkaActualizaEstadoYSaldoDelAtm() {
        RetiroEstadoService estadoService = new RetiroEstadoService();
        SaldoCuentaService saldoService = new SaldoCuentaService();
        RetiroResultadoConsumer consumer = new RetiroResultadoConsumer(estadoService, saldoService);

        estadoService.registrarPendiente("tx-1", 101, new BigDecimal("1000"));
        saldoService.obtenerOCargar(101, () -> new BigDecimal("8000"));

        RetiroResultadoEvent event = new RetiroResultadoEvent(
                "resultado-1",
                "tx-1",
                "RETIRO_APROBADO",
                101,
                new BigDecimal("1000"),
                new BigDecimal("8000"),
                new BigDecimal("7000"),
                "APROBADO",
                "RETIRO_PROCESADO",
                Instant.parse("2026-10-02T22:00:00Z"));

        ConsumerRecord<String, RetiroResultadoEvent> record =
                new ConsumerRecord<>(KafkaTopics.RETIROS_RESULTADOS, 2, 0L, "101", event);

        consumer.consumir(record);

        assertEquals("APROBADO", estadoService.buscar("tx-1").orElseThrow().estado());
        assertEquals(new BigDecimal("7000"), estadoService.buscar("tx-1").orElseThrow().saldoFinal());
        assertEquals(
                new BigDecimal("7000"),
                saldoService.obtenerOCargar(101, () -> new BigDecimal("9999")));
    }
}
