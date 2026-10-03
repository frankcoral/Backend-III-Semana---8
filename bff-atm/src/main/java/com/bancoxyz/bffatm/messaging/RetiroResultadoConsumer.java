package com.bancoxyz.bffatm.messaging;

import com.bancoxyz.bffatm.event.RetiroResultadoEvent;
import com.bancoxyz.bffatm.service.RetiroEstadoService;
import com.bancoxyz.bffatm.service.SaldoCuentaService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RetiroResultadoConsumer {

    private static final Logger log = LoggerFactory.getLogger(RetiroResultadoConsumer.class);

    private final RetiroEstadoService retiroEstadoService;
    private final SaldoCuentaService saldoCuentaService;

    public RetiroResultadoConsumer(
            RetiroEstadoService retiroEstadoService,
            SaldoCuentaService saldoCuentaService) {
        this.retiroEstadoService = retiroEstadoService;
        this.saldoCuentaService = saldoCuentaService;
    }

    @KafkaListener(
            topics = KafkaTopics.RETIROS_RESULTADOS,
            concurrency = "${app.kafka.resultados.concurrency:3}")
    public void consumir(ConsumerRecord<String, RetiroResultadoEvent> record) {
        RetiroResultadoEvent event = record.value();
        retiroEstadoService.registrarResultado(event);

        // Retroalimentación Semana 7:
        // el resultado emitido por Core Banco es la fuente autoritativa del saldo
        // posterior al retiro. Se actualiza la caché local del ATM para evitar
        // responder con un saldo anterior en consultas posteriores.
        saldoCuentaService.actualizarDesdeResultado(event.cuentaId(), event.saldoFinal());

        log.info(
                "[CONSUMER-ATM] Resultado de retiro recibido "
                        + "topic={} partition={} offset={} key={} "
                        + "transaccionId={} estado={} motivo={} "
                        + "saldoAnterior={} saldoFinal={}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                event.transaccionId(),
                event.estado(),
                event.motivo(),
                event.saldoAnterior(),
                event.saldoFinal());
    }
}
