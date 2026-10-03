package com.bancoxyz.core.messaging;

import com.bancoxyz.core.event.RetiroResultadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RetiroResultadoProducer {

    private static final Logger log = LoggerFactory.getLogger(RetiroResultadoProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RetiroResultadoProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicar(RetiroResultadoEvent event) {
        String key = event.cuentaId().toString();

        kafkaTemplate.send(KafkaTopics.RETIROS_RESULTADOS, key, event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error(
                                "[PRODUCER-CORE] Error publicando resultado transaccionId={}",
                                event.transaccionId(),
                                error);
                        return;
                    }

                    log.info(
                            "[PRODUCER-CORE] Resultado publicado "
                                    + "topic={} partition={} offset={} key={} "
                                    + "transaccionId={} estado={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            key,
                            event.transaccionId(),
                            event.estado());
                });
    }
}
