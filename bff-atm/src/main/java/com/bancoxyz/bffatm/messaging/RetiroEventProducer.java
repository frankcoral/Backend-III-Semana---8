package com.bancoxyz.bffatm.messaging;

import com.bancoxyz.bffatm.event.RetiroSolicitadoEvent;
import com.bancoxyz.bffatm.service.RetiroEstadoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class RetiroEventProducer {

    private static final Logger log = LoggerFactory.getLogger(RetiroEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RetiroEstadoService retiroEstadoService;

    public RetiroEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            RetiroEstadoService retiroEstadoService) {
        this.kafkaTemplate = kafkaTemplate;
        this.retiroEstadoService = retiroEstadoService;
    }

    public CompletableFuture<SendResult<String, Object>> publicar(
            RetiroSolicitadoEvent event) {

        /*
         * cuentaId se utiliza como key Kafka.
         * Los retiros de la misma cuenta tenderán a la misma partición,
         * manteniendo su orden relativo.
         */
        String key = event.cuentaId().toString();

        log.info(
                "[PRODUCER] Publicando RETIRO_SOLICITADO " +
                        "transaccionId={} eventId={} cuentaId={} monto={}",
                event.transaccionId(),
                event.eventId(),
                event.cuentaId(),
                event.monto());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaTopics.RETIROS_SOLICITADOS,
                key,
                event);

        future.whenComplete((result, error) -> {

            if (error != null) {
                retiroEstadoService.registrarErrorPublicacion(
                        event.transaccionId(),
                        event.cuentaId(),
                        event.monto());
                log.error(
                        "[PRODUCER] Error publicando RETIRO_SOLICITADO " +
                                "transaccionId={}",
                        event.transaccionId(),
                        error);
                return;
            }

            log.info(
                    "[PRODUCER] RETIRO_SOLICITADO publicado " +
                            "topic={} partition={} offset={} key={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    key);
        });

        return future;
    }
}