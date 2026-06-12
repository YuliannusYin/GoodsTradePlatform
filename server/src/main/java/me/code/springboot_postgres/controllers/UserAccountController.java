package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.requests.*;
import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.UserDetailsDTO;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.services.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/account")
public class UserAccountController {

    private final UserAccountService userAccountService;

    @Autowired
    public UserAccountController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody CreateUserDTO dto) {
        return userAccountService.submitRegistration(dto).toResponseEntity();
    }

    @GetMapping("/details")
    public ResponseEntity<ApiResponse<UserDetailsDTO>> getAccountDetails(@AuthenticationPrincipal User user) {
        return userAccountService.getUserDetails(user).toResponseEntity();
    }

    @PutMapping("/username")
    public ResponseEntity<ApiResponse<Void>> changeUsername(@AuthenticationPrincipal User user, @RequestBody ChangeUsernameDTO dto) {
        return userAccountService.changeUsername(user, dto).toResponseEntity();
    }

    @PutMapping("/email")
    public ResponseEntity<ApiResponse<Void>> changeEmail(@AuthenticationPrincipal User user, @RequestBody ChangeEmailDTO dto) {
        return userAccountService.changeEmail(user, dto).toResponseEntity();
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal User user, @RequestBody ChangePasswordDTO dto) {
        return userAccountService.changePassword(user, dto).toResponseEntity();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@AuthenticationPrincipal User user) {
        return userAccountService.deleteAccount(user).toResponseEntity();
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Boolean>> isValidCredentials(@RequestBody UserLoginDTO dto) {
        boolean valid = userAccountService.isValidUserCredentials(dto.email(), dto.password());
        return ApiResponse.ok("Credentials validated", valid).toResponseEntity();
    }
}
