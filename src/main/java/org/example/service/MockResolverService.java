package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.endpoint.CachedEndpoint;
import org.example.repository.MockEndpointRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MockResolverService {

    private final MockEndpointRepository endpointRepository;

    /**
     * Пошук mock-схеми за хешем користувача, методом і шляхом.
     * Результат (легкий CachedEndpoint) кешується в Redis,
     * бо це гарячий шлях — викликається на КОЖЕН запит до /mock/**.
     * <p>
     * Кешуємо CachedEndpoint, а не Entity, щоб не тягнути LAZY-зв'язку user
     * і безпечно серіалізувати в JSON.
     */
    @Cacheable(value = "mock-endpoints",
            key = "#userHash + ':' + #method + ':' + #path",
            unless = "#result == null")
    @Transactional(readOnly = true)
    public CachedEndpoint resolve(String userHash, String method, String path) {
        return endpointRepository.findByUserHashAndMethodAndPath(userHash, method, path)
                .map(CachedEndpoint::from)
                .orElse(null);
    }
}