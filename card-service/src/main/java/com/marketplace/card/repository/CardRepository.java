package com.marketplace.card.repository;

import com.marketplace.card.model.Card;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CardRepository extends ReactiveMongoRepository<Card, String> {
    Flux<Card> findByGame(String game);
    Flux<Card> findByNameContainingIgnoreCase(String name);
}
