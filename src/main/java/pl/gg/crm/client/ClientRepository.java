package pl.gg.crm.client;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.gg.crm.entity.Client;

public interface ClientRepository extends JpaRepository<Client, Integer> {
}
