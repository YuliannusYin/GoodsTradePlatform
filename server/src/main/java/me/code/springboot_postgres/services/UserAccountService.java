package me.code.springboot_postgres.services;

import me.code.springboot_postgres.dtos.requests.ChangeEmailDTO;
import me.code.springboot_postgres.dtos.requests.ChangePasswordDTO;
import me.code.springboot_postgres.dtos.requests.ChangeUsernameDTO;
import me.code.springboot_postgres.dtos.requests.CreateUserDTO;
import me.code.springboot_postgres.dtos.responses.success.Success;
import me.code.springboot_postgres.dtos.responses.success.variants.UserDetailsSuccess;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAccountService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserAccountService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Success submitRegistration(CreateUserDTO dto) {
        checkUniqueValues(dto.email(), dto.username());
        try {
            String encryptedPassword = passwordEncoder.encode(dto.password());
            User newUser = new User(dto.email(), dto.username(), encryptedPassword, User.Role.USER);
            userRepository.save(newUser);
            return new Success(HttpStatus.CREATED, "Successfully registered a new account");
        } catch (Exception exception) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Could not register a new account");
        }
    }

    private void checkUniqueValues(String email, String username) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomRuntimeException(HttpStatus.CONFLICT, "An account with the chosen email already exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new CustomRuntimeException(HttpStatus.CONFLICT, "An account with the chosen username already exists");
        }
    }

    public Success getUserDetails(User user) {
        try {
            return new UserDetailsSuccess(HttpStatus.OK,
                    "User details were successfully retrieved",
                    user.getEmail(), user.getUsername(),
                    user.getBalance(), user.isProtected(),
                    user.getRole().name());
        } catch (Exception exception) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Could not fetch user details");
        }
    }

    public Success changeUsername(User user, ChangeUsernameDTO dto) {
        checkNotProtected(user, "change username");
        if (userRepository.existsByUsername(dto.newUsername())) {
            throw new CustomRuntimeException(HttpStatus.CONFLICT, "An account with the chosen username already exists");
        }
        try {
            user.setUsername(dto.newUsername());
            userRepository.save(user);
            return new Success(HttpStatus.OK, "The username was successfully changed");
        } catch (Exception exception) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Could not change username");
        }
    }

    public Success changeEmail(User user, ChangeEmailDTO dto) {
        checkNotProtected(user, "change email");
        if (userRepository.existsByEmail(dto.newEmail())) {
            throw new CustomRuntimeException(HttpStatus.CONFLICT, "An account with the chosen email already exists");
        }
        try {
            user.setEmail(dto.newEmail());
            userRepository.save(user);
            return new Success(HttpStatus.OK, "The email was successfully changed");
        } catch (Exception exception) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Could not change email");
        }
    }

    public Success changePassword(User user, ChangePasswordDTO dto) {
        checkNotProtected(user, "change password");
        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        try {
            String encryptedPassword = passwordEncoder.encode(dto.newPassword());
            user.setPassword(encryptedPassword);
            userRepository.save(user);
            return new Success(HttpStatus.OK, "The password was successfully changed");
        } catch (Exception exception) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Could not change password");
        }
    }

    public Success deleteAccount(User user) {
        checkNotProtected(user, "delete account");
        try {
            userRepository.deleteById(user.getId());
            return new Success(HttpStatus.OK, "The account was successfully deleted");
        } catch (Exception exception) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Could not delete account");
        }
    }

    public boolean isValidUserCredentials(String email, String password) {
        User user = loadUserByEmail(email);
        return passwordEncoder.matches(password, user.getPassword());
    }

    public User loadUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Could not find user with id: " + userId));
    }

    public User loadUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Could not find user with email: " + email));
    }

    @Override
    public User loadUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Could not find user with username: " + username));
    }

    private void checkNotProtected(User user, String action) {
        if (user.isProtected()) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "This is a system account. You cannot " + action + ".");
        }
    }
}
