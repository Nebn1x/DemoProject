package aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.example.aop.aspect.LoggingAspect;
import org.example.service.RequestLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @Mock
    private RequestLogService requestLogService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private LoggingAspect loggingAspect;

    @Test
    void shouldLogRequestAndExtractHashSuccessfully() throws Throwable {
        String testUrl = "/mock/a1b2c3d4/api/users";
        when(request.getRequestURI()).thenReturn(testUrl);
        when(request.getMethod()).thenReturn("GET");

        when(joinPoint.proceed()).thenReturn(ResponseEntity.ok().build());

        Object result = loggingAspect.logMockRequest(joinPoint);

        assertEquals(200, ((ResponseEntity<?>) result).getStatusCode().value());

        verify(joinPoint).proceed();

        verify(requestLogService).saveLogAsync(
                eq("a1b2c3d4"),
                eq("GET"),
                eq(testUrl),
                eq(200),
                anyInt()
        );
    }
}