package com.bancoxyz.bffatm.client;

import com.bancoxyz.bffatm.dto.SaldoInicialDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CoreBancoClient {

    private final RestClient restClient;

    public CoreBancoClient(RestClient coreBancoRestClient) {
        this.restClient = coreBancoRestClient;
    }

    /*
     * Semana 8:
     * El JWT se recibe explícitamente para reenviarlo al Core Banco XYZ.
     * Esto evita depender de SecurityContextHolder dentro del hilo usado
     * por Resilience4j/CircuitBreaker.
     */
    public SaldoInicialDto saldoInicial(Integer cuentaId, String accessToken) {
        return restClient.get()
                .uri("/api/core/cuentas/{cuentaId}/saldo-inicial", cuentaId)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .body(SaldoInicialDto.class);
    }

    /*
     * Método legacy conservado temporalmente hasta actualizar AtmService.
     */
    public SaldoInicialDto saldoInicial(Integer cuentaId) {
        return restClient.get()
                .uri("/api/core/cuentas/{cuentaId}/saldo-inicial", cuentaId)
                .retrieve()
                .body(SaldoInicialDto.class);
    }
}
