package com.marketplace.trade.controller;

import com.marketplace.trade.client.CardClient;
import com.marketplace.trade.dto.CardDto;
import com.marketplace.trade.dto.ListingDetailsDto;
import com.marketplace.trade.model.CardListing;
import com.marketplace.trade.repository.ListingRepository;
import com.marketplace.trade.kafka.PurchaseProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades/listings")
@RequiredArgsConstructor
public class TradeController {

    private final ListingRepository listingRepository;
    private final CardClient cardClient;
    private final PurchaseProducer purchaseProducer;

    @PostMapping
    public ResponseEntity<?> createListing(@RequestBody CardListing listing) {
        // Valida se o card existe no catálogo (com circuit breaker ativo)
        boolean cardExists = cardClient.verifyCardExists(listing.getCardId());
        if (!cardExists) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Não foi possível criar o anúncio: Card ID '" + listing.getCardId() + "' não existe no catálogo.");
        }

        listing.setStatus("AVAILABLE");
        CardListing savedListing = listingRepository.save(listing);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedListing);
    }

    @GetMapping
    public List<CardListing> getListings(@RequestParam(required = false) String status) {
        if (status != null) {
            return listingRepository.findByStatus(status.toUpperCase());
        }
        return listingRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingDetailsDto> getListingById(@PathVariable Long id) {
        return listingRepository.findById(id)
                .map(listing -> {
                    CardDto card = cardClient.getCardById(listing.getCardId());
                    return ResponseEntity.ok(new ListingDetailsDto(listing, card));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/buy")
    public ResponseEntity<?> buyListing(@PathVariable Long id) {
        return listingRepository.findById(id)
                .map(listing -> {
                    if ("SOLD".equals(listing.getStatus())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("Erro: Anúncio finalizado. O card já foi vendido.");
                    }
                    listing.setStatus("SOLD");
                    CardListing updated = listingRepository.save(listing);
                    
                    // Publica o evento CompraRealizada no Kafka de forma assíncrona
                    purchaseProducer.publishPurchase(listing.getCardId(), listing.getPrice());
                    
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
