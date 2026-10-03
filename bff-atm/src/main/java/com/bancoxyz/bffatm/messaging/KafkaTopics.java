package com.bancoxyz.bffatm.messaging;

public final class KafkaTopics {

    public static final String RETIROS_SOLICITADOS = "banco.retiros.solicitados";

    public static final String RETIROS_RESULTADOS = "banco.retiros.resultados";

    public static final String RETIROS_DLT = "banco.retiros.dlt";

    private KafkaTopics() {
    }
}