package com.marketplace.trade.dto;

import com.marketplace.trade.model.CardListing;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingDetailsDto {
    private CardListing listing;
    private CardDto cardDetails;
}
