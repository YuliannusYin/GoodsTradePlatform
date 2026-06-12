package me.code.springboot_postgres.controllers;

import me.code.springboot_postgres.dtos.responses.ApiResponse;
import me.code.springboot_postgres.dtos.responses.FavoriteDTO;
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
    public ResponseEntity<ApiResponse<Void>> addFavorite(@AuthenticationPrincipal User user, @RequestParam String productId) {
        return favoriteService.addFavorite(user, productId).toResponseEntity();
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(@AuthenticationPrincipal User user, @PathVariable String productId) {
        return favoriteService.removeFavorite(user, productId).toResponseEntity();
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<FavoriteDTO>>> getUserFavorites(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("Favorites retrieved", favoriteService.getUserFavorites(user.getId())).toResponseEntity();
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> isFavorite(@AuthenticationPrincipal User user, @PathVariable String productId) {
        return ApiResponse.ok("Favorite status checked", favoriteService.isFavorite(user.getId(), productId)).toResponseEntity();
    }
}
