package me.code.springboot_postgres.config;

import me.code.springboot_postgres.models.entities.Role;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.RoleRepository;
import me.code.springboot_postgres.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

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
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ensureUser(SUPER_ADMIN_EMAIL, SUPER_ADMIN_PASSWORD, User.LegacyRole.ADMIN, BigDecimal.ZERO, true, "SUPER_ADMIN");
        ensureUser(MERCHANT_EMAIL, MERCHANT_PASSWORD, User.LegacyRole.USER, BigDecimal.ZERO, true, "USER");
        ensureUser(TEST_USER_EMAIL, TEST_USER_PASSWORD, User.LegacyRole.USER, new BigDecimal("10000000.00"), true, "USER");
    }

    private void ensureUser(String email, String rawPassword, User.LegacyRole legacyRole,
                             BigDecimal balance, boolean isProtected, String rbacRoleName) {
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isEmpty()) {
            // User not in DB (shouldn't happen if V2 ran), create it
            String encoded = passwordEncoder.encode(rawPassword);
            User user = new User(email, email.split("@")[0], encoded, legacyRole, balance, isProtected);
            assignRbacRole(user, rbacRoleName);
            userRepository.save(user);
            log.info("Created built-in user: {} ({})", email, rbacRoleName);
            return;
        }

        User user = existing.get();
        fixPlaceholderPassword(user, rawPassword);
        ensureFields(user, legacyRole, balance, isProtected);
        ensureRbacRole(user, rbacRoleName);
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

    private void ensureFields(User user, User.LegacyRole legacyRole, double balance, boolean isProtected) {
        boolean needsUpdate = false;

        if (user.getLegacyRole() != legacyRole) {
            user.setLegacyRole(legacyRole);
            needsUpdate = true;
        }
        if (user.isProtected() != isProtected) {
            user.setProtected(isProtected);
            needsUpdate = true;
        }
        if (user.getBalance().compareTo(balance) != 0 && balance.compareTo(BigDecimal.ZERO) > 0) {
            user.setBalance(balance);
            needsUpdate = true;
        }

        if (needsUpdate) {
            userRepository.save(user);
            log.info("Updated built-in user fields: {}", user.getEmail());
        }
    }

    private void ensureRbacRole(User user, String rbacRoleName) {
        boolean hasRole = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(rbacRoleName));
        if (!hasRole) {
            assignRbacRole(user, rbacRoleName);
            userRepository.save(user);
            log.info("Assigned RBAC role {} to user: {}", rbacRoleName, user.getEmail());
        }
    }

    private void assignRbacRole(User user, String rbacRoleName) {
        roleRepository.findByName(rbacRoleName).ifPresent(role ->
                user.setRoles(Set.of(role))
        );
    }
}
