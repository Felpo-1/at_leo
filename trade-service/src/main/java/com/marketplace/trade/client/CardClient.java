package com.marketplace.trade.client;

import com.marketplace.trade.dto.CardDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardClient {

    private final RestTemplate restTemplate;

    @CircuitBreaker(name = "cardServiceCB", fallbackMethod = "getCardFallback")
    public CardDto getCardById(String cardId) {
        log.info("Chamando card-service para obter card com id: {}", cardId);
        return restTemplate.getForObject("http://localhost:8081/api/cards/" + cardId, CardDto.class);
    }

    @CircuitBreaker(name = "cardServiceCB", fallbackMethod = "verifyCardFallback")
    public boolean verifyCardExists(String cardId) {
        try {
            log.info("Chamando card-service para verificar card com id: {}", cardId);
            restTemplate.getForEntity("http://localhost:8081/api/cards/" + cardId, CardDto.class);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    // Fallback para getCardById quando o card-service falhar
    public CardDto getCardFallback(String cardId, Throwable throwable) {
        log.warn("Fallback acionado para o cardId: {}. Motivo: {}", cardId, throwable.getMessage());
        CardDto fallbackCard = new CardDto();
        fallbackCard.setId(cardId);
        fallbackCard.setName("Informações do Card Indisponíveis (Fallback)");
        fallbackCard.setGame("Indisponível");
        fallbackCard.setExpansion("Indisponível");
        fallbackCard.setRarity("Indisponível");
        fallbackCard.setAveragePrice(0.0);
        fallbackCard.setAttributes(new HashMap<>());
        fallbackCard.getAttributes().put("error", "Circuito aberto ou falha ao contactar card-service");
        fallbackCard.getAttributes().put("details", throwable.getMessage());
        return fallbackCard;
    }

    // Fallback para verifyCardExists quando o card-service falhar
    public boolean verifyCardFallback(String cardId, Throwable throwable) {
        log.warn("Fallback acionado para verificação do cardId: {}. Permitindo criação com aviso. Motivo: {}", cardId, throwable.getMessage());
        // Retornamos true para não bloquear a operação de cadastro caso o outro microsserviço esteja fora
        return true;
    }
}
