package me.code.springboot_postgres.config;

import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            String encryptedPassword = passwordEncoder.encode("Password");

            User user = new User("user@user.com", "JohnDoe", encryptedPassword, User.Role.USER);
            userRepository.save(user);

            User admin = new User("admin@admin.com", "JaneDoe", encryptedPassword, User.Role.ADMIN);
            userRepository.save(admin);
        }
    }
}
