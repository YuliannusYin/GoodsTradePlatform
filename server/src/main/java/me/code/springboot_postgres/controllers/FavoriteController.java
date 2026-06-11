package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.requests.FavoriteRequestDTO;
import me.code.springboot_postgres.dtos.responses.entities.FavoriteDTO;
import me.code.springboot_postgres.dtos.responses.success.Success;
import me.code.springboot_postgres.models.entities.User;
import me.code.springboot_postgres.services.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Autowired
    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/add")
    public ResponseEntity<Success> addFavorite(@AuthenticationPrincipal User user, @RequestBody FavoriteRequestDTO dto) {
        var result = favoriteService.addFavorite(user, dto.productId());
        return result.toResponseEntity();
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Success> removeFavorite(@AuthenticationPrincipal User user, @PathVariable String productId) {
        var result = favoriteService.removeFavorite(user, productId);
        return result.toResponseEntity();
    }

    @GetMapping("/list")
    public ResponseEntity<List<FavoriteDTO>> getUserFavorites(@AuthenticationPrincipal User user) {
        var result = favoriteService.getUserFavorites(user.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<Boolean> isFavorite(@AuthenticationPrincipal User user, @PathVariable String productId) {
        var result = favoriteService.isFavorite(user.getId(), productId);
        return ResponseEntity.ok(result);
    }
}
