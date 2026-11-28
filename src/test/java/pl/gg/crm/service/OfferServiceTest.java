package pl.gg.crm.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pl.gg.crm.dto.OfferRequest;
import pl.gg.crm.dto.OfferResponse;
import pl.gg.crm.entity.Client;
import pl.gg.crm.entity.Employee;
import pl.gg.crm.entity.Offer;
import pl.gg.crm.repository.OfferRepository;
import pl.gg.crm.security.user.CurrentUserIdResolver;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    @Mock
    private OfferRepository offerRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private CurrentUserIdResolver currentUserIdResolver;

    @InjectMocks
    private OfferService offerService;

    @Test
    void listReturnsPageOfOffers() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(offerRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(offer(1, "OF/1")), pageable, 1));

        Page<OfferResponse> result = offerService.list(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().offerNumber()).isEqualTo("OF/1");
    }

    @Test
    void getMissingOfferReturns404() {
        when(offerRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.get(99))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createWithTakenNumberReturns409() {
        when(offerRepository.existsByOfferNumber("OF/1")).thenReturn(true);

        assertThatThrownBy(() -> offerService.create(request("OF/1"), null))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void deleteRemovesOffer() {
        Offer offer = offer(1, "OF/1");
        when(offerRepository.findById(1)).thenReturn(Optional.of(offer));

        offerService.delete(1);

        verify(offerRepository).delete(offer);
    }

    @Test
    void deleteLinkedOfferReturns409() {
        Offer offer = offer(1, "OF/1");
        when(offerRepository.findById(1)).thenReturn(Optional.of(offer));
        doThrow(new DataIntegrityViolationException("fk")).when(offerRepository).flush();

        assertThatThrownBy(() -> offerService.delete(1))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    private static OfferRequest request(String number) {
        return new OfferRequest(1, number, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 1),
                new BigDecimal("1000.00"), "NOWA", null);
    }

    private static Offer offer(Integer id, String number) {
        Client client = new Client();
        client.setId(1);
        Employee employee = new Employee();
        employee.setId(1);
        Offer offer = new Offer();
        offer.setId(id);
        offer.setClient(client);
        offer.setCreatedBy(employee);
        offer.setOfferNumber(number);
        offer.setOfferDate(LocalDate.of(2026, 11, 1));
        offer.setValidUntil(LocalDate.of(2026, 12, 1));
        offer.setTotalAmount(new BigDecimal("1000.00"));
        offer.setStatus("NOWA");
        return offer;
    }
}
