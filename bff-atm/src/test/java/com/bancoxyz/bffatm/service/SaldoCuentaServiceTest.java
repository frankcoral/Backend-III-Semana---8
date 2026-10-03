package com.bancoxyz.bffatm.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaldoCuentaServiceTest {

    @Test
    void actualizaSaldoCacheadoConResultadoKafka() {
        SaldoCuentaService service = new SaldoCuentaService();
        AtomicInteger cargas = new AtomicInteger();

        BigDecimal inicial = service.obtenerOCargar(101, () -> {
            cargas.incrementAndGet();
            return new BigDecimal("8000");
        });

        assertEquals(new BigDecimal("8000"), inicial);
        assertEquals(1, cargas.get());

        service.actualizarDesdeResultado(101, new BigDecimal("7000"));

        BigDecimal posterior = service.obtenerOCargar(101, () -> {
            cargas.incrementAndGet();
            return new BigDecimal("9999");
        });

        assertEquals(new BigDecimal("7000"), posterior);
        assertEquals(1, cargas.get(), "No debe volver a cargar el saldo desde Core si Kafka ya lo actualizó");
    }
}
