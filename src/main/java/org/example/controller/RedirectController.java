package org.example.controller;

import org.example.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Tag(name = "Redirect", description = "Редирект по коротким ссылкам")
@RestController
public class RedirectController {

    private final UrlShortenerService service;

    public RedirectController(UrlShortenerService service) {
        this.service = service;
    }

    @Operation(summary = "Редирект по коду короткой ссылки")
    @GetMapping("/r/{code}")
    public Object redirect(@PathVariable String code) {
        UrlShortenerService.RedirectResult result = service.redirect(code);
        switch (result.getStatus()) {
            case NOT_FOUND:
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ссылка не найдена");
            case EXPIRED:
                return ResponseEntity.status(HttpStatus.GONE).body("Срок жизни ссылки истёк");
            case LIMIT_REACHED:
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body("Достигнут лимит переходов по ссылке");
            case OK:
                RedirectView rv = new RedirectView(result.getUrl());
                rv.setStatusCode(HttpStatus.FOUND);
                return rv;
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
