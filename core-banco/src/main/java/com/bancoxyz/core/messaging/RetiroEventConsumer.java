package com.bancoxyz.core.messaging;

import com.bancoxyz.core.event.RetiroResultadoEvent;
import com.bancoxyz.core.event.RetiroSolicitadoEvent;
import com.bancoxyz.core.service.RetiroCoreService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RetiroEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(RetiroEventConsumer.class);

    private final RetiroCoreService retiroCoreService;
    private final RetiroResultadoProducer retiroResultadoProducer;

    public RetiroEventConsumer(
            RetiroCoreService retiroCoreService,
            RetiroResultadoProducer retiroResultadoProducer) {
        this.retiroCoreService = retiroCoreService;
        this.retiroResultadoProducer = retiroResultadoProducer;
    }

    @KafkaListener(
            topics = KafkaTopics.RETIROS_SOLICITADOS,
            concurrency = "${app.kafka.retiros.concurrency:3}")
    public void consumir(ConsumerRecord<String, RetiroSolicitadoEvent> record) {
        RetiroSolicitadoEvent event = record.value();

        log.info(
                "[CONSUMER-CORE] RETIRO_SOLICITADO recibido "
                        + "topic={} partition={} offset={} key={} "
                        + "transaccionId={} eventId={} cuentaId={} monto={}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                event.transaccionId(),
                event.eventId(),
                event.cuentaId(),
                event.monto());

        RetiroResultadoEvent resultado = retiroCoreService.procesar(event);

        log.info(
                "[CORE] Retiro procesado "
                        + "transaccionId={} estado={} motivo={} "
                        + "saldoAnterior={} saldoFinal={}",
                resultado.transaccionId(),
                resultado.estado(),
                resultado.motivo(),
                resultado.saldoAnterior(),
                resultado.saldoFinal());

        retiroResultadoProducer.publicar(resultado);
    }
}
