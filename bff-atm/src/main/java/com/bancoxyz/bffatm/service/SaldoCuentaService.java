package com.bancoxyz.bffatm.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Mantiene en memoria el último saldo conocido por cuenta en el BFF ATM.
 *
 * El saldo se carga inicialmente desde Core Banco y luego se sincroniza con
 * los eventos de resultado de retiros recibidos desde Kafka. De esta forma,
 * una consulta posterior no reutiliza un saldo anterior al retiro.
 */
@Service
public class SaldoCuentaService {

    private final Map<Integer, BigDecimal> saldos = new ConcurrentHashMap<>();

    public BigDecimal obtenerOCargar(Integer cuentaId, Supplier<BigDecimal> cargador) {
        return saldos.computeIfAbsent(cuentaId, id -> cargador.get());
    }

    public void actualizarDesdeResultado(Integer cuentaId, BigDecimal saldoFinal) {
        if (cuentaId == null || saldoFinal == null) {
            return;
        }

        saldos.put(cuentaId, saldoFinal);
    }
}
