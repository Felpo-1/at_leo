package com.marketplace.trade.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDto {
    private String id;
    private String name;
    private String game;
    private String expansion;
    private String rarity;
    private Double averagePrice;
    private Map<String, Object> attributes;
}
