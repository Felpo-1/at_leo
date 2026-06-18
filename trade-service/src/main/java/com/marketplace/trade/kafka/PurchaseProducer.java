package com.marketplace.trade.kafka;

import com.marketplace.trade.event.CompraRealizadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseProducer {

    private final KafkaTemplate<String, CompraRealizadaEvent> kafkaTemplate;
    private static final String TOPIC = "trades.purchases";

    public void publishPurchase(String cardId, Double price) {
        // Recupera o Correlation ID do MDC para propagá-lo no payload do evento Kafka
        String correlationId = MDC.get("correlationId");
        CompraRealizadaEvent event = new CompraRealizadaEvent(cardId, price, correlationId);
        
        log.info("Publicando evento no Kafka (Tópico: '{}'): {}", TOPIC, event);
        
        kafkaTemplate.send(TOPIC, cardId, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Erro ao enviar evento de compra para o Kafka: {}", ex.getMessage());
                    } else {
                        log.info("Evento enviado com sucesso para o Kafka! Offset: {}", 
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
