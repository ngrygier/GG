package pl.gg.crm.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.gg.crm.dto.OfferRequest;
import pl.gg.crm.dto.OfferResponse;
import pl.gg.crm.entity.Client;
import pl.gg.crm.entity.Employee;
import pl.gg.crm.entity.Offer;
import pl.gg.crm.repository.OfferRepository;
import pl.gg.crm.security.user.CurrentUserIdResolver;

// Jeszcze jest do zrobienia: uprawnienia wg ról (WF40), wyszukiwanie/filtrowanie (WF24), dokument oferty (WF26).

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;
    private final EntityManager entityManager;
    private final CurrentUserIdResolver currentUserIdResolver;

    // WF21
    @Transactional(readOnly = true)
    public Page<OfferResponse> list(Pageable pageable) {
        return offerRepository.findAll(pageable).map(OfferResponse::from);
    }

    // WF25
    @Transactional(readOnly = true)
    public OfferResponse get(Integer id) {
        return OfferResponse.from(findOffer(id));
    }

    // WF22
    @Transactional
    public OfferResponse create(OfferRequest request, Authentication authentication) {
        if (offerRepository.existsByOfferNumber(request.offerNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Oferta o tym numerze już istnieje");
        }
        Offer offer = new Offer();
        offer.setCreatedBy(currentEmployee(authentication));
        apply(offer, request);
        return OfferResponse.from(offerRepository.save(offer));
    }

    // WF22
    @Transactional
    public OfferResponse update(Integer id, OfferRequest request) {
        Offer offer = findOffer(id);
        if (!offer.getOfferNumber().equals(request.offerNumber())
                && offerRepository.existsByOfferNumber(request.offerNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Oferta o tym numerze już istnieje");
        }
        apply(offer, request);
        return OfferResponse.from(offer);
    }

    // WF23 - usunięcie z bazy; oferta powiązana z innymi rekordami (np. projektem) nie zostanie usunięta
    @Transactional
    public void delete(Integer id) {
        Offer offer = findOffer(id);
        try {
            offerRepository.delete(offer);
            offerRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Nie można usunąć oferty powiązanej z innymi rekordami (np. projektem)");
        }
    }

    private Offer findOffer(Integer id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta nie istnieje"));
    }

    private void apply(Offer offer, OfferRequest request) {
        if (request.validUntil().isBefore(request.offerDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Termin ważności nie może być wcześniejszy niż data oferty");
        }
        Client client = entityManager.find(Client.class, request.clientId());
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wskazany klient nie istnieje");
        }
        offer.setClient(client);
        offer.setOfferNumber(request.offerNumber());
        offer.setOfferDate(request.offerDate());
        offer.setValidUntil(request.validUntil());
        offer.setTotalAmount(request.totalAmount());
        offer.setStatus(request.status());
        offer.setDescription(request.description());
    }

    private Employee currentEmployee(Authentication authentication) {
        Long employeeId = currentUserIdResolver.resolveUserId(authentication)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak zalogowanego użytkownika"));
        Employee employee = entityManager.find(Employee.class, employeeId.intValue());
        if (employee == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Zalogowany użytkownik nie jest pracownikiem");
        }
        return employee;
    }
}
