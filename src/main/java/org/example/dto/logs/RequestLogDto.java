package org.example.dto.logs;

import lombok.Builder;
import org.example.entity.RequestLog;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record RequestLogDto(
        UUID id,
        String method,
        String path,
        Integer status,
        Integer latencyMs,
        LocalDateTime timestamp
) {
    public static RequestLogDto from(RequestLog log) {
        return RequestLogDto.builder()
                .id(log.getId())
                .method(log.getRequestMethod())
                .path(log.getRequestPath())
                .status(log.getResponseStatus())
                .latencyMs(log.getLatencyMs())
                .timestamp(log.getTimestamp())
                .build();
    }
}