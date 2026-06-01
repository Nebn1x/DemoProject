package service;

import org.example.entity.MockEndpoint;
import org.example.entity.RequestLog;
import org.example.repository.MockEndpointRepository;
import org.example.repository.RequestLogRepository;
import org.example.service.RequestLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestLogServiceTest {

    @Mock
    private MockEndpointRepository endpointRepository;

    @Mock
    private RequestLogRepository requestLogRepository;

    @InjectMocks
    private RequestLogService requestLogService;

    @Test
    void shouldSaveLogWhenEndpointExists() {
        String userHash = "123hash";
        String method = "POST";
        String path = "/mock/123hash/data";

        MockEndpoint fakeEndpoint = new MockEndpoint();
        fakeEndpoint.setId(UUID.randomUUID());

        when(endpointRepository.findByUserHashAndMethodAndPath(userHash, method, path))
                .thenReturn(Optional.of(fakeEndpoint));

        requestLogService.saveLogAsync(userHash, method, path, 201, 45);

        verify(requestLogRepository, times(1)).save(any(RequestLog.class));
    }

    @Test
    void shouldNotThrowExceptionWhenEndpointNotFound() {
        when(endpointRepository.findByUserHashAndMethodAndPath(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        requestLogService.saveLogAsync("invalid", "GET", "/path", 404, 10);

        verify(requestLogRepository, never()).save(any());
    }
}