package me.code.springboot_postgres.config;

import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    // Built-in accounts (also inserted by V2 migration, passwords fixed here)
    private static final String SUPER_ADMIN_EMAIL = "admin@merchandise.com";
    private static final String SUPER_ADMIN_PASSWORD = "Admin@2024";

    private static final String MERCHANT_EMAIL = "merchant@merchandise.com";
    private static final String MERCHANT_PASSWORD = "Merchant@2024";

    private static final String TEST_USER_EMAIL = "testuser@merchandise.com";
    private static final String TEST_USER_PASSWORD = "Test@2024";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ensureUser(SUPER_ADMIN_EMAIL, SUPER_ADMIN_PASSWORD, User.Role.ADMIN, 0.0, true);
        ensureUser(MERCHANT_EMAIL, MERCHANT_PASSWORD, User.Role.USER, 0.0, true);
        ensureUser(TEST_USER_EMAIL, TEST_USER_PASSWORD, User.Role.USER, 10_000_000.0, true);
    }

    private void ensureUser(String email, String rawPassword, User.Role role,
                             double balance, boolean isProtected) {
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isEmpty()) {
            // User not in DB (shouldn't happen if V2 ran), create it
            String encoded = passwordEncoder.encode(rawPassword);
            User user = new User(email, email.split("@")[0], encoded, role, balance, isProtected);
            userRepository.save(user);
            log.info("Created built-in user: {} ({})", email, role);
            return;
        }

        User user = existing.get();
        fixPlaceholderPassword(user, rawPassword);
        ensureFields(user, role, balance, isProtected);
    }

    private void fixPlaceholderPassword(User user, String rawPassword) {
        String stored = user.getPassword();
        // Replace placeholder passwords from V2 migration
        if (stored.contains("placeholder")) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            log.info("Fixed placeholder password for: {}", user.getEmail());
            return;
        }
        // Fix corrupted hashes (e.g. missing {bcrypt} prefix)
        try {
            passwordEncoder.matches(rawPassword, stored);
        } catch (IllegalArgumentException e) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            log.warn("Fixed corrupted password hash for: {}", user.getEmail());
        }
    }

    private void ensureFields(User user, User.Role role, double balance, boolean isProtected) {
        boolean needsUpdate = false;

        if (user.getRole() != role) {
            user.setRole(role);
            needsUpdate = true;
        }
        if (user.isProtected() != isProtected) {
            user.setProtected(isProtected);
            needsUpdate = true;
        }
        if (Double.compare(user.getBalance(), balance) != 0 && balance > 0) {
            user.setBalance(balance);
            needsUpdate = true;
        }

        if (needsUpdate) {
            userRepository.save(user);
            log.info("Updated built-in user fields: {}", user.getEmail());
        }
    }
}
