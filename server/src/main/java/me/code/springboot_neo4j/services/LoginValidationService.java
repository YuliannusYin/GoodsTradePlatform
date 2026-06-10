package me.code.springboot_neo4j.services;

import me.code.springboot_neo4j.dtos.requests.UserLoginDTO;
import me.code.springboot_neo4j.dtos.responses.error.details.ValidationErrorDetail;
import me.code.springboot_neo4j.exceptions.types.variants.ValidationException;
import me.code.springboot_neo4j.models.nodes.User;
import me.code.springboot_neo4j.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginValidationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public LoginValidationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void validateUserCredentials(UserLoginDTO dto) {
        String email = dto.email();
        String password = dto.password();

        if (isInvalidEmail(email)) {
            throw new ValidationException(
                    HttpStatus.BAD_REQUEST,
                    "You have entered an invalid email",
                    getValidationErrorDetail(email));

        } else if (isInvalidPassword(email, password)) {
            throw new ValidationException(
                    HttpStatus.BAD_REQUEST,
                    "You have entered an invalid password",
                    getValidationErrorDetail());
        }
    }

    private boolean isInvalidEmail(String email) {
        return userRepository.isInvalidEmail(email);
    }

    private boolean isInvalidPassword(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.isEmpty() || !passwordEncoder.matches(password, user.get().getPassword());
    }

    private ValidationErrorDetail getValidationErrorDetail() {
        return new ValidationErrorDetail(
                "Is not a valid password",
                "JSON",
                "password",
                "Hidden"
        );
    }

    private ValidationErrorDetail getValidationErrorDetail(String email) {
        return new ValidationErrorDetail(
                "Is not a valid email",
                "JSON",
                "email",
                email);
    }
}
