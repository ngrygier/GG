package pl.gg.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.gg.crm.entity.Offer;

public interface OfferRepository extends JpaRepository<Offer, Integer> {

    boolean existsByOfferNumber(String offerNumber);
}
