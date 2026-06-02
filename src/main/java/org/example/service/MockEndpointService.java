package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.endpoint.CreateEndpointRequest;
import org.example.dto.endpoint.EndpointResponse;
import org.example.dto.endpoint.UpdateEndpointRequest;
import org.example.entity.MockEndpoint;
import org.example.entity.User;
import org.example.exception.EndpointAlreadyExistsException;
import org.example.exception.EndpointNotFoundException;
import org.example.repository.MockEndpointRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MockEndpointService {

    private final MockEndpointRepository endpointRepository;
    private final UserRepository userRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @CacheEvict(value = "mock-endpoints", allEntries = true)
    @Transactional
    public EndpointResponse create(UUID userId, CreateEndpointRequest req) {
        if (endpointRepository.existsByUser_IdAndMethodAndPath(userId, req.method(), req.path())) {
            throw new EndpointAlreadyExistsException(req.method(), req.path());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EndpointNotFoundException("Користувач не знайдений"));

        LocalDateTime expiresAt = (req.ttlHours() != null && req.ttlHours() > 0)
                ? LocalDateTime.now().plusHours(req.ttlHours())
                : null;

        MockEndpoint endpoint = MockEndpoint.builder()
                .user(user)
                .method(req.method())
                .path(req.path())
                .responseBody(req.responseBody())
                .responseStatus(req.responseStatus() != null ? req.responseStatus() : 200)
                .contentType(req.contentType() != null ? req.contentType() : "application/json")
                .delayMs(req.delayMs() != null ? req.delayMs() : 0)
                .expiresAt(expiresAt)
                .build();

        endpoint = endpointRepository.save(endpoint);
        log.info("Створено ендпоінт {} {} для користувача {}", req.method(), req.path(), userId);

        return EndpointResponse.from(endpoint, baseUrl);
    }

    @Transactional(readOnly = true)
    public Page<EndpointResponse> list(UUID userId, Pageable pageable) {
        return endpointRepository.findByUser_Id(userId, pageable)
                .map(e -> EndpointResponse.from(e, baseUrl));
    }

    @Transactional(readOnly = true)
    public EndpointResponse getById(UUID userId, UUID endpointId) {
        MockEndpoint endpoint = findOwnedEndpoint(userId, endpointId);
        return EndpointResponse.from(endpoint, baseUrl);
    }

    @CacheEvict(value = "mock-endpoints", allEntries = true)
    @Transactional
    public EndpointResponse update(UUID userId, UUID endpointId, UpdateEndpointRequest req) {
        MockEndpoint endpoint = findOwnedEndpoint(userId, endpointId);

        if (req.method() != null)         endpoint.setMethod(req.method());
        if (req.path() != null)           endpoint.setPath(req.path());
        if (req.responseBody() != null)   endpoint.setResponseBody(req.responseBody());
        if (req.responseStatus() != null) endpoint.setResponseStatus(req.responseStatus());
        if (req.contentType() != null)    endpoint.setContentType(req.contentType());
        if (req.delayMs() != null)        endpoint.setDelayMs(req.delayMs());

        endpoint = endpointRepository.save(endpoint);
        log.info("Оновлено ендпоінт {}", endpointId);

        return EndpointResponse.from(endpoint, baseUrl);
    }

    @CacheEvict(value = "mock-endpoints", allEntries = true)
    @Transactional
    public void delete(UUID userId, UUID endpointId) {
        MockEndpoint endpoint = findOwnedEndpoint(userId, endpointId);
        endpointRepository.delete(endpoint);
        log.info("Видалено ендпоінт {}", endpointId);
    }

    /**
     * Знаходить ендпоінт і перевіряє, що він належить цьому користувачу.
     * Захист від доступу до чужих ендпоінтів.
     */
    private MockEndpoint findOwnedEndpoint(UUID userId, UUID endpointId) {
        MockEndpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(() -> new EndpointNotFoundException("Ендпоінт не знайдений: " + endpointId));

        if (!endpoint.getUser().getId().equals(userId)) {
            throw new EndpointNotFoundException("Ендпоінт не знайдений: " + endpointId);
        }
        return endpoint;
    }
}