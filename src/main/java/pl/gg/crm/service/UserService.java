package pl.gg.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.gg.crm.entity.User;
import pl.gg.crm.repository.UserRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(String username,
                           String rawPassword,
                           String email) {

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Username already exists"
            );
        }

        User user = new User();

        user.setUsername(username);
        user.setPasswordHash(
                passwordEncoder.encode(rawPassword)
        );
        user.setEmail(email);
        user.setIsActive(true);
        user.setCreatedAt(Instant.now());

        return userRepository.save(user);
    }
}
