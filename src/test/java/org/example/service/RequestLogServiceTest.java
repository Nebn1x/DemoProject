package org.example.service;

import org.example.entity.MockEndpoint;
import org.example.entity.RequestLog;
import org.example.repository.MockEndpointRepository;
import org.example.repository.RequestLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestLogService")
class RequestLogServiceTest {

    @Mock
    private MockEndpointRepository endpointRepository;

    @Mock
    private RequestLogRepository requestLogRepository;

    @InjectMocks
    private RequestLogService requestLogService;

    @Test
    @DisplayName("зберігає лог, коли ендпоінт існує")
    void shouldSaveLogWhenEndpointExists() {
        String userHash = "123hash";
        String method = "POST";
        // path БЕЗ префікса /mock/{hash} - так зберігається MockEndpoint.path
        String path = "/data";

        MockEndpoint fakeEndpoint = new MockEndpoint();
        fakeEndpoint.setId(UUID.randomUUID());

        when(endpointRepository.findByUserHashAndMethodAndPath(userHash, method, path))
                .thenReturn(Optional.of(fakeEndpoint));

        requestLogService.saveLogAsync(userHash, method, path, 201, 45);

        verify(requestLogRepository, times(1)).save(any(RequestLog.class));
    }

    @Test
    @DisplayName("не падає, коли ендпоінт не знайдено")
    void shouldNotThrowExceptionWhenEndpointNotFound() {
        when(endpointRepository.findByUserHashAndMethodAndPath(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        requestLogService.saveLogAsync("invalid", "GET", "/path", 404, 10);

        verify(requestLogRepository, never()).save(any());
    }
}
