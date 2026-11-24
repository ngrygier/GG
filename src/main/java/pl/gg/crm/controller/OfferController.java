package pl.gg.crm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.gg.crm.dto.OfferRequest;
import pl.gg.crm.dto.OfferResponse;
import pl.gg.crm.service.OfferService;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    @GetMapping
    public PagedModel<OfferResponse> list(Pageable pageable) {
        return new PagedModel<>(offerService.list(pageable));
    }

    @GetMapping("/{id}")
    public OfferResponse get(@PathVariable Integer id) {
        return offerService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfferResponse create(@Valid @RequestBody OfferRequest request, Authentication authentication) {
        return offerService.create(request, authentication);
    }

    @PutMapping("/{id}")
    public OfferResponse update(@PathVariable Integer id, @Valid @RequestBody OfferRequest request) {
        return offerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        offerService.delete(id);
    }
}
