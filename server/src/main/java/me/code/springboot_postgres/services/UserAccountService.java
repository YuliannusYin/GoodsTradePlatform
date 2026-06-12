package me.code.springboot_postgres.services;

import me.code.springboot_postgres.dtos.requests.ChangeEmailDTO;
import me.code.springboot_postgres.dtos.requests.ChangePasswordDTO;
import me.code.springboot_postgres.dtos.requests.ChangeUsernameDTO;
import me.code.springboot_postgres.dtos.requests.CreateUserDTO;
import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.UserDetailsDTO;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserAccountService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ApiResponse<Void> submitRegistration(CreateUserDTO dto) {
        checkUniqueValues(dto.email(), dto.username());
        String encryptedPassword = passwordEncoder.encode(dto.password());
        User newUser = new User(dto.email(), dto.username(), encryptedPassword, User.Role.USER);
        userRepository.save(newUser);
        return ApiResponse.created("Successfully registered a new account", null);
    }

    private void checkUniqueValues(String email, String username) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomRuntimeException(HttpStatus.CONFLICT, "An account with the chosen email already exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new CustomRuntimeException(HttpStatus.CONFLICT, "An account with the chosen username already exists");
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<UserDetailsDTO> getUserDetails(User user) {
        UserDetailsDTO dto = new UserDetailsDTO(
                user.getEmail(), user.getUsername(), user.getBalance(), user.isProtected(), user.getRole().name());
        return ApiResponse.ok("User details were successfully retrieved", dto);
    }

    @Transactional
    public ApiResponse<Void> changeUsername(User user, ChangeUsernameDTO dto) {
        checkNotProtected(user, "change username");
        if (userRepository.existsByUsername(dto.newUsername())) {
            throw new CustomRuntimeException(HttpStatus.CONFLICT, "An account with the chosen username already exists");
        }
        user.setUsername(dto.newUsername());
        userRepository.save(user);
        return ApiResponse.ok("The username was successfully changed");
    }

    @Transactional
    public ApiResponse<Void> changeEmail(User user, ChangeEmailDTO dto) {
        checkNotProtected(user, "change email");
        if (userRepository.existsByEmail(dto.newEmail())) {
            throw new CustomRuntimeException(HttpStatus.CONFLICT, "An account with the chosen email already exists");
        }
        user.setEmail(dto.newEmail());
        userRepository.save(user);
        return ApiResponse.ok("The email was successfully changed");
    }

    @Transactional
    public ApiResponse<Void> changePassword(User user, ChangePasswordDTO dto) {
        checkNotProtected(user, "change password");
        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new CustomRuntimeException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
        return ApiResponse.ok("The password was successfully changed");
    }

    @Transactional
    public ApiResponse<Void> deleteAccount(User user) {
        checkNotProtected(user, "delete account");
        userRepository.deleteById(user.getId());
        return ApiResponse.ok("The account was successfully deleted");
    }

    @Transactional(readOnly = true)
    public boolean isValidUserCredentials(String email, String password) {
        User user = loadUserByEmail(email);
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Transactional(readOnly = true)
    public User loadUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Could not find user with id: " + userId));
    }

    @Transactional(readOnly = true)
    public User loadUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "Could not find user with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("Could not find user with username: " + username));
    }

    private void checkNotProtected(User user, String action) {
        if (user.isProtected()) {
            throw new CustomRuntimeException(HttpStatus.FORBIDDEN, "This is a system account. You cannot " + action + ".");
        }
    }
}
