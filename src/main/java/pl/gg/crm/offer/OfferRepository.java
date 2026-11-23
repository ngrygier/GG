package pl.gg.crm.offer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pl.gg.crm.entity.Offer;

import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Integer>, JpaSpecificationExecutor<Offer> {

    @Override
    @EntityGraph(attributePaths = "client")
    Optional<Offer> findById(Integer id);

    @Override
    @EntityGraph(attributePaths = "client")
    Page<Offer> findAll(Specification<Offer> spec, Pageable pageable);
}
