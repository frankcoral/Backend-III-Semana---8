package com.bancoxyz.bffweb.client;

import com.bancoxyz.bffweb.dto.CuentaInteresDto;
import com.bancoxyz.bffweb.dto.MovimientoAnualDto;
import com.bancoxyz.bffweb.dto.TransaccionDto;
import com.bancoxyz.bffweb.dto.WebResumenDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class CoreBancoClient {

    private final RestClient restClient;

    public CoreBancoClient(RestClient coreBancoRestClient) {
        this.restClient = coreBancoRestClient;
    }

    /**
     * Semana 8:
     * el Bearer Token se recibe explícitamente como parámetro. Esto evita
     * depender de SecurityContextHolder dentro del hilo usado por
     * Resilience4j/CircuitBreaker.
     */
    public WebResumenDto resumen(String accessToken) {
        return restClient.get()
                .uri("/api/core/resumen")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .body(WebResumenDto.class);
    }

    public List<TransaccionDto> transacciones(int limit, String accessToken) {
        return restClient.get()
                .uri(uri -> uri.path("/api/core/transacciones")
                        .queryParam("limit", limit)
                        .build())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public List<CuentaInteresDto> intereses(int limit, String accessToken) {
        return restClient.get()
                .uri(uri -> uri.path("/api/core/intereses")
                        .queryParam("limit", limit)
                        .build())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public List<MovimientoAnualDto> movimientos(int limit, String accessToken) {
        return restClient.get()
                .uri(uri -> uri.path("/api/core/movimientos")
                        .queryParam("limit", limit)
                        .build())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
