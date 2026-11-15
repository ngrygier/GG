package pl.gg.crm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;
import pl.gg.crm.security.access.ResourceAccessService;

import static org.assertj.core.api.Assertions.assertThat;

// TODO(DB): projekt nie ma jeszcze konfiguracji bazy danych (ani testowej, np. Testcontainers), więc pełny kontekst
// nie wstanie z DataSource/JPA. Usunąć wykluczenia, gdy osoba odpowiedzialna za bazę doda konfigurację testową.
@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
        + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration")
class CrmApplicationTests {

	@Autowired
	private SecurityFilterChain securityFilterChain;

	@Autowired
	private ResourceAccessService resourceAccessService;

	@Test
	void contextLoads() {
		assertThat(securityFilterChain).isNotNull();
		assertThat(resourceAccessService).isNotNull();
	}

}
