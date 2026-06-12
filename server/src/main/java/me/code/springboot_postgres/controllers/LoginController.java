package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.requests.UserLoginDTO;
import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.AuthenticationDTO;
import me.code.springboot_postgres.exceptions.types.CustomRuntimeException;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.security.JwtTokenUtil;
import me.code.springboot_postgres.services.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/account")
public class LoginController {

    private final AuthenticationProvider authenticationProvider;
    private final UserAccountService userAccountService;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public LoginController(AuthenticationProvider authenticationProvider,
                           UserAccountService userAccountService,
                           JwtTokenUtil jwtTokenUtil) {
        this.authenticationProvider = authenticationProvider;
        this.userAccountService = userAccountService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationDTO>> login(@RequestBody UserLoginDTO dto) {
        User user = authenticateUser(dto);
        String token = jwtTokenUtil.generateToken(user);
        AuthenticationDTO data = new AuthenticationDTO(List.of(user.getRole().name()), token);
        return ApiResponse.ok("Login successful", data).toResponseEntity();
    }

    private User authenticateUser(UserLoginDTO dto) {
        User user = userAccountService.loadUserByEmail(dto.email());
        var token = new UsernamePasswordAuthenticationToken(user.getUsername(), dto.password());
        Authentication result;
        try {
            result = authenticationProvider.authenticate(token);
        } catch (Exception e) {
            throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        if (!result.isAuthenticated()) {
            throw new CustomRuntimeException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return user;
    }
}
