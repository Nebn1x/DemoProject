package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.logs.RequestLogDto;
import org.example.security.UserPrincipal;
import org.example.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/me")
    public ResponseEntity<List<RequestLogDto>> getMyStats(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // Дістаємо логи для поточного юзера
        List<RequestLogDto> logs = statsService.getMyLogs(principal.getId());
        return ResponseEntity.ok(logs);
    }
}