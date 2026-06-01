package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.entity.MockEndpoint;
import org.example.entity.RequestLog;
import org.example.repository.MockEndpointRepository;
import org.example.repository.RequestLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/mock")
@RequiredArgsConstructor
public class MockController {

    private final MockEndpointRepository endpointRepository;
    private final RequestLogRepository requestLogRepository;

    @Transactional
    @RequestMapping("/{userHash}/**")
    public ResponseEntity<String> handleMockRequest(
            @PathVariable String userHash,
            HttpServletRequest request
    ) {
        // 1. Отримуємо точний шлях (наприклад, "/api/test-data")
        String requestUri = request.getRequestURI();
        String mockPath = requestUri.substring(("/mock/" + userHash).length());

        // 2. Знаходимо ендпоінт у базі (через стрім, щоб не міняти твій репозиторій)
        MockEndpoint endpoint = endpointRepository.findAll().stream()
                .filter(e -> e.getUser().getUserHash().equals(userHash) && e.getPath().equals(mockPath))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Ендпоінт не знайдено"));

        // 3. ЗБЕРІГАЄМО ЛОГ! (Те, через що падав тест статистики)
        RequestLog log = new RequestLog();
        log.setEndpoint(endpoint);
        log.setRequestMethod(request.getMethod());
        log.setRequestPath(mockPath);
        log.setResponseStatus(endpoint.getResponseStatus());
        log.setTimestamp(LocalDateTime.now());

        // Зберігаємо запис про виклик в базу
        requestLogRepository.save(log);

        // 4. Повертаємо імітовану відповідь
        return ResponseEntity.status(endpoint.getResponseStatus())
                .body(endpoint.getResponseBody());
    }
}