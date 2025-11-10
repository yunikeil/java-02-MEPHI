package org.example.service;

import org.example.config.AppSettings;
import org.example.model.ShortLink;
import org.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UrlShortenerService {

    private static final Logger log = LoggerFactory.getLogger(UrlShortenerService.class);

    public enum RedirectStatus {
        OK,
        NOT_FOUND,
        EXPIRED,
        LIMIT_REACHED
    }

    public static class RedirectResult {
        private final RedirectStatus status;
        private final String url;

        public RedirectResult(RedirectStatus status, String url) {
            this.status = status;
            this.url = url;
        }

        public RedirectStatus getStatus() {
            return status;
        }

        public String getUrl() {
            return url;
        }
    }

    private final Map<UUID, User> users = new ConcurrentHashMap<>();
    private final Map<String, ShortLink> linksByCode = new ConcurrentHashMap<>();
    private final Map<UUID, List<ShortLink>> linksByUser = new ConcurrentHashMap<>();

    private final long ttlMillis;
    private final int defaultMaxClicks;

    private final SecureRandom random = new SecureRandom();
    private static final String ALPHABET =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;

    public UrlShortenerService(AppSettings settings) {
        this.ttlMillis = settings.getTtlMillis();
        this.defaultMaxClicks = settings.getMaxClicks();
    }

    // ===== Пользователи =====

    public synchronized User createUser(String name) {
        User user = new User(name);
        users.put(user.getId(), user);
        linksByUser.put(user.getId(), new ArrayList<>());
        return user;
    }

    public Optional<User> findUserByName(String name) {
        return users.values().stream()
                .filter(u -> u.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public Optional<User> findUserById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }

    // ===== Ссылки =====

    public synchronized ShortLink createShortLink(UUID ownerId, String originalUrl) {
        if (!users.containsKey(ownerId)) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        String code = generateUniqueCode();
        ShortLink link = new ShortLink(ownerId, originalUrl, code, ttlMillis, defaultMaxClicks);
        linksByCode.put(code, link);
        linksByUser.computeIfAbsent(ownerId, k -> new ArrayList<>()).add(link);
        return link;
    }

    public synchronized List<ShortLink> getLinksForUser(UUID userId) {
        return new ArrayList<>(linksByUser.getOrDefault(userId, Collections.emptyList()));
    }

    public synchronized boolean editShortLink(UUID ownerId, String code, String newUrl) {
        ShortLink link = linksByCode.get(code);
        if (link == null) return false;
        if (!link.getOwnerId().equals(ownerId)) return false;
        link.setOriginalUrl(newUrl);
        return true;
    }

    public synchronized boolean deleteShortLink(UUID ownerId, String code) {
        ShortLink link = linksByCode.get(code);
        if (link == null) return false;
        if (!link.getOwnerId().equals(ownerId)) return false;
        removeLink(link);
        return true;
    }

    // изменение лимита/TTL владельцем
    public synchronized boolean updateSettings(UUID ownerId,
                                               String code,
                                               Integer maxClicks,
                                               Long ttlSeconds) {
        ShortLink link = linksByCode.get(code);
        if (link == null) return false;
        if (!link.getOwnerId().equals(ownerId)) return false;

        if (maxClicks != null) {
            link.setMaxClicks(maxClicks);
        }
        if (ttlSeconds != null) {
            link.setTtlMillis(ttlSeconds * 1000L);
        }
        return true;
    }

    public synchronized RedirectResult redirect(String code) {
        ShortLink link = linksByCode.get(code);
        if (link == null) {
            return new RedirectResult(RedirectStatus.NOT_FOUND, null);
        }

        if (link.isExpired()) {
            log.info("TTL expired for link code={} owner={}", code, link.getOwnerId());
            removeLink(link);
            return new RedirectResult(RedirectStatus.EXPIRED, null);
        }

        if (link.isLimitReached()) {
            log.info("Click limit reached for link code={} owner={}", code, link.getOwnerId());
            return new RedirectResult(RedirectStatus.LIMIT_REACHED, null);
        }

        link.incrementClick();
        return new RedirectResult(RedirectStatus.OK, link.getOriginalUrl());
    }

    public synchronized void cleanupExpired() {
        List<ShortLink> toRemove = new ArrayList<>();
        for (ShortLink link : linksByCode.values()) {
            if (link.isExpired()) {
                toRemove.add(link);
            }
        }
        for (ShortLink link : toRemove) {
            log.info("Cleanup expired link code={} owner={}", link.getCode(), link.getOwnerId());
            removeLink(link);
        }
    }

    private synchronized void removeLink(ShortLink link) {
        linksByCode.remove(link.getCode());
        List<ShortLink> userLinks = linksByUser.get(link.getOwnerId());
        if (userLinks != null) {
            userLinks.removeIf(l -> l.getCode().equals(link.getCode()));
        }
    }

    private String generateUniqueCode() {
        while (true) {
            String code = randomCode();
            if (!linksByCode.containsKey(code)) {
                return code;
            }
        }
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int idx = random.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(idx));
        }
        return sb.toString();
    }
}
