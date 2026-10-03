package com.bancoxyz.bffatm.controller;

import com.bancoxyz.bffatm.dto.AtmBalanceDto;
import com.bancoxyz.bffatm.dto.RetiroAceptadoDto;
import com.bancoxyz.bffatm.dto.RetiroEstadoDto;
import com.bancoxyz.bffatm.dto.RetiroRequest;
import com.bancoxyz.bffatm.service.AtmService;
import com.bancoxyz.bffatm.service.RetiroEstadoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/atm")
public class AtmController {
    private final AtmService service;
    private final RetiroEstadoService retiroEstadoService;

    public AtmController(AtmService service, RetiroEstadoService retiroEstadoService) {
        this.service = service;
        this.retiroEstadoService = retiroEstadoService;
    }

    @GetMapping("/cuentas/{cuentaId}/saldo")
    public AtmBalanceDto saldo(@PathVariable Integer cuentaId) {
        return service.consultarSaldo(cuentaId);
    }

    @GetMapping("/retiros/{transaccionId}")
    public ResponseEntity<RetiroEstadoDto> estadoRetiro(@PathVariable String transaccionId) {
        return retiroEstadoService.buscar(transaccionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/cuentas/{cuentaId}/retiros")
    public ResponseEntity<RetiroAceptadoDto> retirar(
            @PathVariable Integer cuentaId,
            @Valid @RequestBody RetiroRequest request) {

        RetiroAceptadoDto response =
                service.solicitarRetiro(
                        cuentaId,
                        request.monto());

        return ResponseEntity
                .accepted()
                .body(response);
    }
}
