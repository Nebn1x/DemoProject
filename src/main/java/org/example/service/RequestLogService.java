package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.MockEndpoint;
import org.example.entity.RequestLog;
import org.example.entity.User;
import org.example.exception.EndpointNotFoundException;
import org.example.exception.UserNotFoundException;
import org.example.repository.MockEndpointRepository;
import org.example.repository.RequestLogRepository;
import org.example.repository.UserRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestLogService {

    private final RequestLogRepository requestLogRepository;
    private final UserRepository userRepository;
    private final MockEndpointRepository endpointRepository;

    @Async
    @Transactional
    public void saveLogAsync(String userHash, String method, String path, int statusCode, int latencyMs) {
        try {
            MockEndpoint endpoint = endpointRepository.findByUserHashAndMethodAndPath(userHash, method, path).orElseThrow(() -> new EndpointNotFoundException("Endpoint not found for logging"));
            RequestLog requestLog = RequestLog.builder()
                    .endpoint(endpoint)
                    .requestMethod(method)
                    .requestPath(path)
                    .responseStatus(statusCode)
                    .latencyMs(latencyMs)
                    .build();
            requestLogRepository.save(requestLog);
        } catch (Exception e) {
            log.error("Log error {}: {}", path, e.getMessage());
        }
    }
}
