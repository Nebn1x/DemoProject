package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.logs.RequestLogDto;
import org.example.entity.MockEndpoint;
import org.example.entity.RequestLog;
import org.example.repository.MockEndpointRepository;
import org.example.repository.RequestLogRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestLogService {

    private final RequestLogRepository requestLogRepository;
    private final MockEndpointRepository endpointRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Асинхронний запис логу виклику mock.
     * path тут — БЕЗ префікса /mock/{userHash}, як зберігається MockEndpoint.path.
     */
    @Async
    @Transactional
    public void saveLogAsync(String userHash, String method, String path, int statusCode, int latencyMs) {
        try {
            MockEndpoint endpoint = endpointRepository
                    .findByUserHashAndMethodAndPath(userHash, method, path)
                    .orElse(null);

            if (endpoint == null) {
                // Викликали неіснуючий mock — логувати нема до чого прив'язати, пропускаємо
                log.debug("Лог пропущено: ендпоінт {} {} (hash={}) не знайдено", method, path, userHash);
                return;
            }

            RequestLog requestLog = RequestLog.builder()
                    .endpoint(endpoint)
                    .requestMethod(method)
                    .requestPath(path)
                    .responseStatus(statusCode)
                    .latencyMs(latencyMs)
                    .build();

            requestLog = requestLogRepository.save(requestLog);

            RequestLogDto dto = RequestLogDto.from(requestLog);
            simpMessagingTemplate.convertAndSend("/topic/logs/", dto);

            if (endpoint.getUser() != null) {
                simpMessagingTemplate.convertAndSendToUser(endpoint.getUser().getId().toString(), "/queue/logs", dto);
            }

        } catch (Exception e) {
            log.error("Помилка запису логу {} {}: {}", method, path, e.getMessage());
        }
    }
}