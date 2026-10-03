package com.bancoxyz.bffmobile.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /**
     * Builder normal y primario.
     * Evita que clientes internos como Eureka intenten resolver "localhost"
     * mediante Spring Cloud LoadBalancer.
     */
    @Bean
    @Primary
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Builder integrado con Spring Cloud LoadBalancer.
     * El nombre lógico "core-banco" será resuelto dinámicamente mediante Eureka.
     */
    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    RestClient coreBancoRestClient(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder loadBalancedRestClientBuilder) {

        return loadBalancedRestClientBuilder
                .baseUrl("http://core-banco")
                .build();
    }
}
