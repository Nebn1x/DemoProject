package org.example.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.dto.logs.RequestLogDto;
import org.example.entity.RequestLog;
import org.example.repository.RequestLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final RequestLogRepository requestLogRepository;

    @Transactional(readOnly = true)
    public List<RequestLogDto> getMyLogs(UUID userId) {
        return requestLogRepository.findAllByUserId(userId)
                .stream()
                .map(RequestLogDto::from)
                .toList();
    }
}
