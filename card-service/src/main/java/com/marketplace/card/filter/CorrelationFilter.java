package com.marketplace.card.filter;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationFilter implements WebFilter {

    private static final String CORRELATION_HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Adiciona o cabeçalho de correlação na resposta
        exchange.getResponse().getHeaders().add(CORRELATION_HEADER, correlationId);

        final String finalId = correlationId;
        
        // Propaga no pipeline reativo e ativa o MDC temporariamente para registros de log
        return chain.filter(exchange)
                .contextWrite(context -> context.put(MDC_KEY, finalId))
                .doOnEach(signal -> MDC.put(MDC_KEY, finalId))
                .doFinally(signalType -> MDC.remove(MDC_KEY));
    }
}
