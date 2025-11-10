package org.example.controller;

import org.example.config.AppSettings;
import org.example.dto.CreateLinkRequest;
import org.example.dto.LinkResponse;
import org.example.dto.UpdateLinkRequest;
import org.example.dto.UpdateLinkSettingsRequest;
import org.example.model.ShortLink;
import org.example.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "Links", description = "Операции с короткими ссылками")
@RestController
@RequestMapping("/api/links")
public class LinkController {

    private final UrlShortenerService service;
    private final AppSettings settings;

    public LinkController(UrlShortenerService service, AppSettings settings) {
        this.service = service;
        this.settings = settings;
    }

    private UUID authUserId(String cookieValue) {
        if (cookieValue == null || cookieValue.isBlank()) return null;
        try {
            return UUID.fromString(cookieValue);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isValidUrl(String url) {
        try {
            URI uri = new URI(url);
            if (uri.getScheme() == null) return false;
            String scheme = uri.getScheme().toLowerCase();
            return scheme.equals("http") || scheme.equals("https");
        } catch (URISyntaxException e) {
            return false;
        }
    }

    @Operation(summary = "Создать короткую ссылку")
    @PostMapping
    public ResponseEntity<?> create(
            @CookieValue(name = "userId", required = false) String userIdCookie,
            @Valid @RequestBody CreateLinkRequest request) {

        UUID userId = authUserId(userIdCookie);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Сначала выполните /api/users/register или /api/users/login");
        }

        if (!isValidUrl(request.getUrl())) {
            return ResponseEntity.badRequest()
                    .body("Невалидный URL. Ожидается http(s)://...");
        }

        ShortLink link = service.createShortLink(userId, request.getUrl());
        String fullShortUrl = settings.getBaseUrl() + "/r/" + link.getCode();

        LinkResponse response = new LinkResponse(
                link.getId(),
                fullShortUrl,
                link.getOriginalUrl(),
                link.getClickCount(),
                link.getMaxClicks(),
                link.isExpired()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Список ссылок текущего пользователя")
    @GetMapping
    public ResponseEntity<?> list(
            @CookieValue(name = "userId", required = false) String userIdCookie) {

        UUID userId = authUserId(userIdCookie);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Нет cookie userId. Сначала залогиньтесь.");
        }

        List<ShortLink> links = service.getLinksForUser(userId);
        List<LinkResponse> response = links.stream()
                .map(l -> new LinkResponse(
                        l.getId(),
                        settings.getBaseUrl() + "/r/" + l.getCode(),
                        l.getOriginalUrl(),
                        l.getClickCount(),
                        l.getMaxClicks(),
                        l.isExpired()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Редактировать URL ссылки (только владелец)")
    @PutMapping("/{code}")
    public ResponseEntity<?> update(
            @CookieValue(name = "userId", required = false) String userIdCookie,
            @PathVariable String code,
            @Valid @RequestBody UpdateLinkRequest request) {

        UUID userId = authUserId(userIdCookie);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Нет cookie userId. Сначала залогиньтесь.");
        }

        if (!isValidUrl(request.getUrl())) {
            return ResponseEntity.badRequest()
                    .body("Невалидный URL. Ожидается http(s)://...");
        }

        boolean ok = service.editShortLink(userId, code, request.getUrl());
        if (!ok) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Нет прав или ссылка не найдена");
        }
        return ResponseEntity.ok("URL обновлён");
    }

    @Operation(summary = "Изменить лимит и TTL ссылки (только владелец)")
    @PatchMapping("/{code}/settings")
    public ResponseEntity<?> updateSettings(
            @CookieValue(name = "userId", required = false) String userIdCookie,
            @PathVariable String code,
            @Valid @RequestBody UpdateLinkSettingsRequest request) {

        UUID userId = authUserId(userIdCookie);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Нет cookie userId. Сначала залогиньтесь.");
        }

        if (request.getMaxClicks() == null && request.getTtlSeconds() == null) {
            return ResponseEntity.badRequest().body("Нечего менять — передайте maxClicks и/или ttlSeconds");
        }

        boolean ok = service.updateSettings(userId, code,
                request.getMaxClicks(),
                request.getTtlSeconds());
        if (!ok) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Нет прав или ссылка не найдена");
        }
        return ResponseEntity.ok("Настройки обновлены");
    }

    @Operation(summary = "Удалить ссылку (только владелец)")
    @DeleteMapping("/{code}")
    public ResponseEntity<?> delete(
            @CookieValue(name = "userId", required = false) String userIdCookie,
            @PathVariable String code) {

        UUID userId = authUserId(userIdCookie);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Нет cookie userId. Сначала залогиньтесь.");
        }

        boolean ok = service.deleteShortLink(userId, code);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Нет прав или ссылка не найдена");
        }
        return ResponseEntity.ok("Ссылка удалена");
    }
}
