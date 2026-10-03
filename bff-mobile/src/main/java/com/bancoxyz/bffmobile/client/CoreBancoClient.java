package com.bancoxyz.bffmobile.client;

import com.bancoxyz.bffmobile.dto.MovimientoAnualDto;
import com.bancoxyz.bffmobile.dto.ResumenEstadosAnualesDto;
import com.bancoxyz.bffmobile.dto.ResumenTransaccionesDto;
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

    /*
     * Semana 8:
     * El Bearer Token se recibe explícitamente como parámetro.
     * Esto evita depender de SecurityContextHolder dentro del hilo
     * utilizado por Resilience4j/CircuitBreaker.
     */

    public ResumenTransaccionesDto resumenTransacciones(String accessToken) {
        return restClient.get()
                .uri("/api/core/resumen/transacciones")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .body(ResumenTransaccionesDto.class);
    }

    public ResumenEstadosAnualesDto resumenEstadosAnuales(String accessToken) {
        return restClient.get()
                .uri("/api/core/resumen/estados-anuales")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .body(ResumenEstadosAnualesDto.class);
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

    /*
     * Métodos anteriores conservados temporalmente para no romper
     * referencias hasta actualizar el servicio Mobile en el siguiente paso.
     */
    public ResumenTransaccionesDto resumenTransacciones() {
        return restClient.get()
                .uri("/api/core/resumen/transacciones")
                .retrieve()
                .body(ResumenTransaccionesDto.class);
    }

    public ResumenEstadosAnualesDto resumenEstadosAnuales() {
        return restClient.get()
                .uri("/api/core/resumen/estados-anuales")
                .retrieve()
                .body(ResumenEstadosAnualesDto.class);
    }

    public List<MovimientoAnualDto> movimientos(int limit) {
        return restClient.get()
                .uri(uri -> uri.path("/api/core/movimientos")
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
