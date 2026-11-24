package pl.gg.crm.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OfferRequest(
        @NotNull(message = "Klient jest wymagany")
        Integer clientId,

        @NotBlank(message = "Numer oferty jest wymagany")
        @Size(max = 50, message = "Numer oferty może mieć maksymalnie 50 znaków")
        String offerNumber,

        @NotNull(message = "Data oferty jest wymagana")
        LocalDate offerDate,

        @NotNull(message = "Termin ważności jest wymagany")
        LocalDate validUntil,

        @NotNull(message = "Kwota jest wymagana")
        @DecimalMin(value = "0.00", message = "Kwota nie może być ujemna")
        BigDecimal totalAmount,

        @NotBlank(message = "Status jest wymagany")
        @Size(max = 30, message = "Status może mieć maksymalnie 30 znaków")
        String status,

        String description
) {
}
