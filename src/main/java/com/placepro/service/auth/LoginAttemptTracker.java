package com.placepro.service.auth;

import com.placepro.service.ServiceException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class LoginAttemptTracker {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCKOUT_WINDOW = Duration.ofMinutes(10);

    private final Map<String, List<Instant>> attemptsByAccount = new ConcurrentHashMap<>();

    void recordFailedAttempt(String accountKey) {
        attemptsByAccount.compute(accountKey, (key, attempts) -> {
            List<Instant> updatedAttempts = pruneOldAttempts(attempts);
            updatedAttempts.add(Instant.now());
            return updatedAttempts;
        });
    }

    void clearAttempts(String accountKey) {
        attemptsByAccount.remove(accountKey);
    }

    void ensureNotLocked(String accountKey) {
        List<Instant> recentAttempts = pruneOldAttempts(attemptsByAccount.get(accountKey));
        if (recentAttempts.size() >= MAX_ATTEMPTS) {
            throw new ServiceException("Account temporarily locked due to too many failed login attempts. Try again later.");
        }
    }

    private List<Instant> pruneOldAttempts(List<Instant> attempts) {
        if (attempts == null) {
            return new ArrayList<>();
        }
        Instant cutoff = Instant.now().minus(LOCKOUT_WINDOW);
        List<Instant> recentAttempts = new ArrayList<>();
        for (Instant attempt : attempts) {
            if (attempt.isAfter(cutoff)) {
                recentAttempts.add(attempt);
            }
        }
        return recentAttempts;
    }
}
