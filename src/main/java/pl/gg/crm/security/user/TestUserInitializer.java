package pl.gg.crm.security.user;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pl.gg.crm.repository.UserRepository;
import pl.gg.crm.service.UserService;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class TestUserInitializer {

    private final UserService userService;

    @Bean
    CommandLineRunner createTestUser(
            UserService userService,
            UserRepository userRepository
    ) {
        return args -> {
            if (userRepository.existsByUsername("test")) {
                return;
            }

            userService.createUser(
                    "test3",
                    "dupa",
                    "test3@test.pl"
            );
        };
    }
}


