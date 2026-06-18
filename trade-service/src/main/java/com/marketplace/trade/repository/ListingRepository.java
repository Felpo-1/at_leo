package com.marketplace.trade.repository;

import com.marketplace.trade.model.CardListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<CardListing, Long> {
    List<CardListing> findByStatus(String status);
}
