package pl.gg.crm.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pl.gg.crm.entity.Project;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Integer>, JpaSpecificationExecutor<Project> {

    @Override
    @EntityGraph(attributePaths = "client")
    Page<Project> findAll(Specification<Project> spec, Pageable pageable);

    boolean existsByOfferId(Integer offerId);

    List<Project> findByOfferIdOrderByIdAsc(Integer offerId);
}
