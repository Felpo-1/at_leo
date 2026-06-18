package com.marketplace.card.kafka;

import com.marketplace.card.event.CompraRealizadaEvent;
import com.marketplace.card.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseConsumer {

    private final CardRepository cardRepository;

    @KafkaListener(topics = "trades.purchases", groupId = "card-service-group")
    public void consumePurchase(CompraRealizadaEvent event) {
        // Captura o correlationId do evento Kafka e o insere no MDC do logger
        String cid = event.getCorrelationId();
        if (cid != null) {
            MDC.put("correlationId", cid);
        }

        log.info("Evento CompraRealizada recebido do Kafka: {}", event);

        // Bug #6 fix: Usamos .block() aqui em vez de .subscribe() porque estamos
        // dentro de uma thread do Kafka Listener (que já é uma thread separada do event loop).
        // Com .subscribe() + finally { MDC.remove }, o MDC era limpo ANTES da
        // execução do pipeline reativo, perdendo o correlationId nos logs internos.
        // .block() garante que o pipeline reativo execute completamente nesta thread
        // antes de limpar o MDC.
        try {
            cardRepository.findById(event.getCardId())
                    .flatMap(card -> {
                        log.info("Processando evento: Atualizando preço médio de '{}'. Preço antigo: {}, Preço de venda: {}",
                                card.getName(), card.getAveragePrice(), event.getPrice());

                        double currentAvg = card.getAveragePrice() != null ? card.getAveragePrice() : 0.0;
                        // Média móvel simples simulada
                        double newAvg = Math.round(((currentAvg + event.getPrice()) / 2.0) * 100.0) / 100.0;
                        card.setAveragePrice(newAvg);

                        return cardRepository.save(card);
                    })
                    .doOnSuccess(saved -> log.info("Sucesso! Preço médio do card '{}' atualizado para: {}", saved.getName(), saved.getAveragePrice()))
                    .doOnError(err -> log.error("Falha ao processar evento de compra no card-service: {}", err.getMessage()))
                    .block(); // Bloqueia na thread do Kafka Listener para garantir MDC + execução completa
        } catch (Exception e) {
            log.error("Erro inesperado ao consumir evento do Kafka para cardId '{}': {}", event.getCardId(), e.getMessage());
        } finally {
            MDC.remove("correlationId");
        }
    }
}
