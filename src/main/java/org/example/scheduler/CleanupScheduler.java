package org.example.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.repository.MockEndpointRepository;
import org.example.repository.RequestLogRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Фонові задачі очистки. Вмикаються через @EnableScheduling (в Main).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupScheduler {

    private final MockEndpointRepository endpointRepository;
    private final RequestLogRepository requestLogRepository;

    /**
     * Раз на годину видаляє прострочені mock-ендпоінти (expiresAt < now).
     * Чистимо кеш, бо видалені ендпоінти могли там лишитись.
     * fixedRate = 3600000 мс = 1 година.
     */
    @Scheduled(fixedRate = 3_600_000)
    @CacheEvict(value = "mock-endpoints", allEntries = true)
    @Transactional
    public void cleanupExpiredEndpoints() {
        int deleted = endpointRepository.deleteExpired(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleanup: видалено {} прострочених ендпоінтів", deleted);
        }
    }

    /**
     * Раз на добу видаляє логи, старші за 7 днів.
     * cron: щодня о 03:00 ночі.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        int deleted = requestLogRepository.deleteOlderThan(cutoff);
        if (deleted > 0) {
            log.info("Cleanup: видалено {} старих логів (старші 7 днів)", deleted);
        }
    }
}