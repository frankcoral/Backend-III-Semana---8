package com.bancoxyz.bffweb.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /**
     * Builder normal (sin LoadBalancer).
     * Se mantiene como @Primary para que componentes de infraestructura,
     * como el cliente de Eureka, puedan usar URLs directas sin que el host
     * sea interpretado como un nombre lógico de servicio.
     */
    @Bean
    @Primary
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Builder exclusivo para llamadas entre microservicios mediante
     * Eureka + Spring Cloud LoadBalancer.
     */
    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Cliente hacia Core Banco usando el nombre lógico registrado en Eureka.
     * El JWT se agrega explícitamente en CoreBancoClient antes de entrar a
     * las llamadas protegidas por Resilience4j.
     */
    @Bean
    RestClient coreBancoRestClient(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder loadBalancedRestClientBuilder) {
        return loadBalancedRestClientBuilder
                .baseUrl("http://core-banco")
                .build();
    }
}
