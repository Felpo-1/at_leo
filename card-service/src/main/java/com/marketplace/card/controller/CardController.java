package com.marketplace.card.controller;

import com.marketplace.card.model.Card;
import com.marketplace.card.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardRepository cardRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Card> createCard(@RequestBody Card card) {
        return cardRepository.save(card);
    }

    @GetMapping
    public Flux<Card> getCards(
            @RequestParam(required = false) String game,
            @RequestParam(required = false) String name) {
        if (game != null) {
            return cardRepository.findByGame(game);
        } else if (name != null) {
            return cardRepository.findByNameContainingIgnoreCase(name);
        }
        return cardRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Card>> getCardById(@PathVariable String id) {
        return cardRepository.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
