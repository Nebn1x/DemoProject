package org.example.dto.logs; // або org.example.dto.stats

public record StatsResponse(
        String path,
        int status
) {}