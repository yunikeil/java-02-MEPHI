package org.example.controller;

import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.model.User;
import org.example.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Tag(name = "Users", description = "Регистрация и вход пользователей")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UrlShortenerService service;

    public UserController(UrlShortenerService service) {
        this.service = service;
    }

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest request,
                                                 HttpServletResponse response) {
        User user = service.createUser(request.getName());
        setUserCookie(response, user.getId());
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getName()));
    }

    @Operation(summary = "Вход по имени пользователя")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserRequest request,
                                   HttpServletResponse response) {
        Optional<User> opt = service.findUserByName(request.getName());
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body("Пользователь не найден");
        }
        User user = opt.get();
        setUserCookie(response, user.getId());
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getName()));
    }

    private void setUserCookie(HttpServletResponse response, UUID userId) {
        Cookie cookie = new Cookie("userId", userId.toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
