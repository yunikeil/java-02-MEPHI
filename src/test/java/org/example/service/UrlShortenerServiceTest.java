package org.example.service;

import org.example.config.AppSettings;
import org.example.model.ShortLink;
import org.example.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UrlShortenerServiceTest {

    private UrlShortenerService service;

    @BeforeEach
    void setUp() {
        AppSettings settings = new AppSettings(
                3_600_000L,
                3,
                60_000L,
                "http://localhost:8080"
        );
        service = new UrlShortenerService(settings);
    }

    @Test
    void createUserAndShortLink_shouldStoreLinkForUser() {
        User user = service.createUser("alice");
        ShortLink link = service.createShortLink(user.getId(), "https://example.com");

        assertNotNull(link.getId());
        assertEquals(user.getId(), link.getOwnerId());

        List<ShortLink> links = service.getLinksForUser(user.getId());
        assertEquals(1, links.size());
        assertEquals("https://example.com", links.get(0).getOriginalUrl());
    }

    @Test
    void redirect_shouldIncreaseClickCountUntilLimit_thenBlock() {
        // лимит 2 клика для наглядности
        AppSettings settings = new AppSettings(3_600_000L, 2, 60_000L, "http://localhost:8080");
        service = new UrlShortenerService(settings);

        User user = service.createUser("bob");
        ShortLink link = service.createShortLink(user.getId(), "https://example.com");
        String code = link.getCode();

        UrlShortenerService.RedirectResult r1 = service.redirect(code);
        UrlShortenerService.RedirectResult r2 = service.redirect(code);
        UrlShortenerService.RedirectResult r3 = service.redirect(code);

        assertEquals(UrlShortenerService.RedirectStatus.OK, r1.getStatus());
        assertEquals(UrlShortenerService.RedirectStatus.OK, r2.getStatus());
        assertEquals(UrlShortenerService.RedirectStatus.LIMIT_REACHED, r3.getStatus());

        List<ShortLink> links = service.getLinksForUser(user.getId());
        assertEquals(2, links.get(0).getClickCount());
        assertTrue(links.get(0).isLimitReached());
    }

    @Test
    void redirect_shouldReturnExpired_whenTtlPassed() throws InterruptedException {
        // TTL 50 мс
        AppSettings settings = new AppSettings(50L, 10, 60_000L, "http://localhost:8080");
        service = new UrlShortenerService(settings);

        User user = service.createUser("carol");
        ShortLink link = service.createShortLink(user.getId(), "https://example.com");
        String code = link.getCode();

        // ждём, пока TTL истечёт
        Thread.sleep(80L);

        UrlShortenerService.RedirectResult result = service.redirect(code);
        assertEquals(UrlShortenerService.RedirectStatus.EXPIRED, result.getStatus());

        // после истечения TTL ссылка должна удаляться
        UrlShortenerService.RedirectResult second = service.redirect(code);
        assertEquals(UrlShortenerService.RedirectStatus.NOT_FOUND, second.getStatus());
    }

    @Test
    void editShortLink_shouldBeAllowedOnlyForOwner() {
        User alice = service.createUser("alice");
        User bob = service.createUser("bob");

        ShortLink link = service.createShortLink(alice.getId(), "https://old.example.com");
        String code = link.getCode();

        // владелец может менять
        boolean editedByOwner = service.editShortLink(alice.getId(), code, "https://new.example.com");
        assertTrue(editedByOwner);
        assertEquals("https://new.example.com",
                service.getLinksForUser(alice.getId()).get(0).getOriginalUrl());

        // другой пользователь не может менять
        boolean editedByOther = service.editShortLink(bob.getId(), code, "https://hack.example.com");
        assertFalse(editedByOther);
        assertEquals("https://new.example.com",
                service.getLinksForUser(alice.getId()).get(0).getOriginalUrl());
    }

    @Test
    void deleteShortLink_shouldRemoveLinkOnlyForOwner() {
        User alice = service.createUser("alice");
        User bob = service.createUser("bob");

        ShortLink link = service.createShortLink(alice.getId(), "https://example.com");
        String code = link.getCode();

        // чужой удалить не может
        boolean deletedByOther = service.deleteShortLink(bob.getId(), code);
        assertFalse(deletedByOther);
        assertEquals(1, service.getLinksForUser(alice.getId()).size());

        // владелец удалить может
        boolean deletedByOwner = service.deleteShortLink(alice.getId(), code);
        assertTrue(deletedByOwner);
        assertEquals(0, service.getLinksForUser(alice.getId()).size());
    }

    @Test
    void updateSettings_shouldChangeLimitAndTtlForOwnerOnly() throws InterruptedException {
        AppSettings settings = new AppSettings(10_000L, 1, 60_000L, "http://localhost:8080");
        service = new UrlShortenerService(settings);

        User owner = service.createUser("owner");
        User other = service.createUser("other");

        ShortLink link = service.createShortLink(owner.getId(), "https://example.com");
        String code = link.getCode();

        // другой пользователь не может менять настройки
        boolean updatedByOther = service.updateSettings(other.getId(), code, 5, 1L);
        assertFalse(updatedByOther);

        // владелец может
        boolean updatedByOwner = service.updateSettings(owner.getId(), code, 3, 1L);
        assertTrue(updatedByOwner);

        ShortLink updated = service.getLinksForUser(owner.getId()).get(0);
        assertEquals(3, updated.getMaxClicks());
        assertEquals(1_000L, updated.getTtlMillis());

        // TTL 1 секунда: через ~1.1 секунды ссылка станет EXPIRED
        Thread.sleep(1100L);
        UrlShortenerService.RedirectResult r = service.redirect(code);
        assertEquals(UrlShortenerService.RedirectStatus.EXPIRED, r.getStatus());
    }

    @Test
    void cleanupExpired_shouldRemoveExpiredLinks() throws InterruptedException {
        AppSettings settings = new AppSettings(50L, 10, 60_000L, "http://localhost:8080");
        service = new UrlShortenerService(settings);

        User user = service.createUser("alice");
        ShortLink link1 = service.createShortLink(user.getId(), "https://a.example.com");
        ShortLink link2 = service.createShortLink(user.getId(), "https://b.example.com");

        Thread.sleep(80L);
        service.cleanupExpired();

        List<ShortLink> links = service.getLinksForUser(user.getId());
        assertTrue(links.isEmpty());

        UrlShortenerService.RedirectResult r1 = service.redirect(link1.getCode());
        UrlShortenerService.RedirectResult r2 = service.redirect(link2.getCode());
        assertEquals(UrlShortenerService.RedirectStatus.NOT_FOUND, r1.getStatus());
        assertEquals(UrlShortenerService.RedirectStatus.NOT_FOUND, r2.getStatus());
    }

    @Test
    void createShortLink_shouldFailForUnknownUser() {
        UUID randomUserId = UUID.randomUUID();
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.createShortLink(randomUserId, "https://example.com")
        );
        assertTrue(ex.getMessage().contains("Пользователь не найден"));
    }
}
