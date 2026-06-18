package com.marketplace.trade.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraRealizadaEvent {
    private String cardId;
    private Double price;
    private String correlationId;
}
