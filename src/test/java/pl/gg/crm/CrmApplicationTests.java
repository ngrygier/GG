package pl.gg.crm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.gg.crm.repository.EmployeeRepository;
import pl.gg.crm.repository.UserRepository;
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

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private EmployeeRepository employeeRepository;

	@MockitoBean
	private JwtEncoder jwtEncoder;

	@Test
	void contextLoads() {
		assertThat(securityFilterChain).isNotNull();
		assertThat(resourceAccessService).isNotNull();
	}

}
