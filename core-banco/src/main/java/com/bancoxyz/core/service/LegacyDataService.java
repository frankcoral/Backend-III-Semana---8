package com.bancoxyz.core.service;

import com.bancoxyz.core.model.CuentaInteresLegacy;
import com.bancoxyz.core.model.MovimientoAnualLegacy;
import com.bancoxyz.core.model.TransaccionLegacy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LegacyDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyDataService.class);

    private final List<TransaccionLegacy> transacciones;
    private final List<CuentaInteresLegacy> intereses;
    private final List<MovimientoAnualLegacy> movimientosAnuales;

    public LegacyDataService() {
        this.transacciones = Collections.unmodifiableList(cargarTransacciones());
        this.intereses = Collections.unmodifiableList(cargarIntereses());
        this.movimientosAnuales = Collections.unmodifiableList(cargarMovimientosAnuales());
        LOGGER.info("LEGACY_DATA -> transacciones={}, intereses={}, cuentasAnuales={}",
                transacciones.size(), intereses.size(), movimientosAnuales.size());
    }

    public List<TransaccionLegacy> getTransacciones() {
        return transacciones;
    }

    public List<CuentaInteresLegacy> getIntereses() {
        return intereses;
    }

    public List<MovimientoAnualLegacy> getMovimientosAnuales() {
        return movimientosAnuales;
    }

    public Map<String, Object> resumenCompleto() {
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("transacciones", resumenTransacciones());
        resumen.put("intereses", resumenIntereses());
        resumen.put("estadosAnuales", resumenMovimientosAnuales());
        return resumen;
    }

    public Map<String, Object> resumenTransacciones() {
        long creditos = transacciones.stream().filter(t -> "credito".equalsIgnoreCase(t.tipo())).count();
        long debitos = transacciones.stream().filter(t -> "debito".equalsIgnoreCase(t.tipo())).count();
        long otros = transacciones.size() - creditos - debitos;
        long montosAusentes = transacciones.stream().filter(t -> t.monto() == null).count();
        BigDecimal montoTotal = transacciones.stream()
                .map(TransaccionLegacy::monto)
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("registros", transacciones.size());
        resumen.put("creditos", creditos);
        resumen.put("debitos", debitos);
        resumen.put("otrosTipos", otros);
        resumen.put("montosAusentes", montosAusentes);
        resumen.put("montoTotalInformado", montoTotal);
        return resumen;
    }

    public Map<String, Object> resumenIntereses() {
        long saldosAusentes = intereses.stream().filter(i -> i.saldo() == null).count();
        long edadesAusentes = intereses.stream().filter(i -> i.edad() == null).count();
        long ahorro = intereses.stream().filter(i -> "ahorro".equalsIgnoreCase(i.tipo())).count();
        long prestamo = intereses.stream().filter(i -> "prestamo".equalsIgnoreCase(i.tipo())).count();
        long hipoteca = intereses.stream().filter(i -> "hipoteca".equalsIgnoreCase(i.tipo())).count();
        long otros = intereses.size() - ahorro - prestamo - hipoteca;

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("registros", intereses.size());
        resumen.put("cuentasDistintas", intereses.stream().map(CuentaInteresLegacy::cuentaId).distinct().count());
        resumen.put("ahorro", ahorro);
        resumen.put("prestamo", prestamo);
        resumen.put("hipoteca", hipoteca);
        resumen.put("otrosTipos", otros);
        resumen.put("saldosAusentes", saldosAusentes);
        resumen.put("edadesAusentes", edadesAusentes);
        return resumen;
    }

    public Map<String, Object> resumenMovimientosAnuales() {
        long depositos = movimientosAnuales.stream().filter(m -> "deposito".equalsIgnoreCase(m.transaccion())).count();
        long retiros = movimientosAnuales.stream().filter(m -> "retiro".equalsIgnoreCase(m.transaccion())).count();
        long compras = movimientosAnuales.stream().filter(m -> "compra".equalsIgnoreCase(m.transaccion())).count();
        long otros = movimientosAnuales.size() - depositos - retiros - compras;
        long montosAusentes = movimientosAnuales.stream().filter(m -> m.monto() == null).count();

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("registros", movimientosAnuales.size());
        resumen.put("cuentasDistintas", movimientosAnuales.stream().map(MovimientoAnualLegacy::cuentaId).distinct().count());
        resumen.put("depositos", depositos);
        resumen.put("retiros", retiros);
        resumen.put("compras", compras);
        resumen.put("otrosTipos", otros);
        resumen.put("montosAusentes", montosAusentes);
        return resumen;
    }

    public BigDecimal saldoOperativoInicial(Integer cuentaId) {
        BigDecimal saldo = null;
        for (CuentaInteresLegacy cuenta : intereses) {
            if (cuenta.cuentaId().equals(cuentaId) && cuenta.saldo() != null && cuenta.saldo().signum() >= 0) {
                saldo = cuenta.saldo();
            }
        }
        return saldo;
    }

    public List<MovimientoAnualLegacy> movimientosCuenta(Integer cuentaId, int limite) {
        return movimientosAnuales.stream()
                .filter(m -> m.cuentaId().equals(cuentaId))
                .limit(Math.max(0, limite))
                .toList();
    }

    private List<TransaccionLegacy> cargarTransacciones() {
        List<TransaccionLegacy> resultado = new ArrayList<>();
        for (String[] fila : leerCsv("data/transacciones.csv", 4)) {
            resultado.add(new TransaccionLegacy(
                    entero(fila[0]),
                    fila[1],
                    decimal(fila[2]),
                    fila[3]));
        }
        return resultado;
    }

    private List<CuentaInteresLegacy> cargarIntereses() {
        List<CuentaInteresLegacy> resultado = new ArrayList<>();
        for (String[] fila : leerCsv("data/intereses.csv", 5)) {
            resultado.add(new CuentaInteresLegacy(
                    entero(fila[0]),
                    fila[1],
                    decimal(fila[2]),
                    entero(fila[3]),
                    fila[4]));
        }
        return resultado;
    }

    private List<MovimientoAnualLegacy> cargarMovimientosAnuales() {
        List<MovimientoAnualLegacy> resultado = new ArrayList<>();
        for (String[] fila : leerCsv("data/cuentas_anuales.csv", 5)) {
            resultado.add(new MovimientoAnualLegacy(
                    entero(fila[0]),
                    fila[1],
                    fila[2],
                    decimal(fila[3]),
                    fila[4]));
        }
        return resultado;
    }

    private List<String[]> leerCsv(String ruta, int columnasEsperadas) {
        ClassPathResource resource = new ClassPathResource(ruta);
        List<String[]> filas = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            boolean cabecera = true;
            while ((linea = reader.readLine()) != null) {
                if (cabecera) {
                    cabecera = false;
                    continue;
                }
                if (linea.isBlank()) {
                    continue;
                }
                String[] columnas = linea.split(",", -1);
                if (columnas.length == columnasEsperadas) {
                    filas.add(columnas);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible leer el archivo oficial " + ruta, ex);
        }
        return filas;
    }

    private Integer entero(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private BigDecimal decimal(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(valor.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
