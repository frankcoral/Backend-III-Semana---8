package com.bancoxyz.core.controller;

import com.bancoxyz.core.model.CuentaInteresLegacy;
import com.bancoxyz.core.model.MovimientoAnualLegacy;
import com.bancoxyz.core.model.TransaccionLegacy;
import com.bancoxyz.core.service.LegacyDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/core")
public class CoreBancoController {

    private final LegacyDataService dataService;

    public CoreBancoController(LegacyDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/resumen")
    public Map<String, Object> resumenCompleto() {
        return dataService.resumenCompleto();
    }

    @GetMapping("/resumen/transacciones")
    public Map<String, Object> resumenTransacciones() {
        return dataService.resumenTransacciones();
    }

    @GetMapping("/resumen/estados-anuales")
    public Map<String, Object> resumenEstadosAnuales() {
        return dataService.resumenMovimientosAnuales();
    }

    @GetMapping("/transacciones")
    public List<TransaccionLegacy> transacciones(@RequestParam(defaultValue = "10") int limit) {
        return dataService.getTransacciones().stream().limit(normalizarLimite(limit)).toList();
    }

    @GetMapping("/intereses")
    public List<CuentaInteresLegacy> intereses(@RequestParam(defaultValue = "10") int limit) {
        return dataService.getIntereses().stream().limit(normalizarLimite(limit)).toList();
    }

    @GetMapping("/movimientos")
    public List<MovimientoAnualLegacy> movimientos(@RequestParam(defaultValue = "10") int limit) {
        return dataService.getMovimientosAnuales().stream().limit(normalizarLimite(limit)).toList();
    }

    @GetMapping("/cuentas/{cuentaId}/saldo-inicial")
    public Map<String, Object> saldoInicial(@PathVariable Integer cuentaId) {
        BigDecimal saldo = dataService.saldoOperativoInicial(cuentaId);
        if (saldo == null) {
            throw new IllegalArgumentException("No existe un saldo valido para la cuenta " + cuentaId);
        }
        return Map.of("cuentaId", cuentaId, "saldo", saldo);
    }

    private int normalizarLimite(int limit) {
        return Math.max(0, Math.min(limit, 100));
    }
}
