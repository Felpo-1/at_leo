package com.marketplace.card.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @Id
    private String id;
    private String name;
    private String game; // Ex: "Magic: The Gathering", "Yu-Gi-Oh!", "Pokémon"
    private String expansion; // Ex: "Alpha", "Base Set", "Legend of Blue Eyes"
    private String rarity; // Ex: "Common", "Rare", "Secret Rare"
    private Double averagePrice;
    
    // Atributos dinâmicos e flexíveis dependendo do tipo de jogo (Ataque, Defesa, HP, Custo de Mana, Estrelas, etc.)
    private Map<String, Object> attributes;
}
