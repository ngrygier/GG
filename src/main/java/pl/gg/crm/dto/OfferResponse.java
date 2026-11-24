package pl.gg.crm.dto;

import pl.gg.crm.entity.Offer;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OfferResponse(
        Integer id,
        Integer clientId,
        Integer createdById,
        String offerNumber,
        LocalDate offerDate,
        LocalDate validUntil,
        BigDecimal totalAmount,
        String status,
        String description
) {

    public static OfferResponse from(Offer offer) {
        return new OfferResponse(
                offer.getId(),
                offer.getClient().getId(),
                offer.getCreatedBy().getId(),
                offer.getOfferNumber(),
                offer.getOfferDate(),
                offer.getValidUntil(),
                offer.getTotalAmount(),
                offer.getStatus(),
                offer.getDescription()
        );
    }
}
