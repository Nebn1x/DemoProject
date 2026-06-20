package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.endpoint.CreateEndpointRequest;
import org.example.dto.endpoint.EndpointResponse;
import org.example.dto.endpoint.UpdateEndpointRequest;
import org.example.dto.logs.RequestLogDto;
import org.example.security.UserPrincipal;
import org.example.service.MockEndpointService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/endpoints")
@RequiredArgsConstructor
public class MockEndpointController {

    private final MockEndpointService endpointService;

    @PostMapping
    public ResponseEntity<EndpointResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateEndpointRequest request
    ) {
        EndpointResponse response = endpointService.create(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<EndpointResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(endpointService.list(principal.getId(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EndpointResponse> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(endpointService.getById(principal.getId(), id));
    }

    /**
     * Останні запити до ендпоінта (для сторінки деталей /endpoints/:id).
     * Початкове завантаження сторінки + "сторінка N" пагінація;
     * нові логи в реальному часі приходять окремо через WebSocket /topic/logs/{id}.
     */
    @GetMapping("/{id}/logs")
    public ResponseEntity<Page<RequestLogDto>> getLogs(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(endpointService.getLogs(principal.getId(), id, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EndpointResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEndpointRequest request
    ) {
        return ResponseEntity.ok(endpointService.update(principal.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        endpointService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}