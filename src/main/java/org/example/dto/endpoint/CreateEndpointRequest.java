package org.example.dto.endpoint;

import jakarta.validation.constraints.*;

public record CreateEndpointRequest(

        @NotBlank(message = "HTTP метод обов'язковий")
        @Pattern(regexp = "GET|POST|PUT|PATCH|DELETE",
                message = "Метод має бути одним з: GET, POST, PUT, PATCH, DELETE")
        String method,

        @NotBlank(message = "Шлях обов'язковий")
        @Pattern(regexp = "^/[a-zA-Z0-9/_\\-.]*$",
                message = "Шлях має починатись з / і містити лише літери, цифри, /, _, -, .")
        @Size(max = 500)
        String path,

        @Size(max = 1048576, message = "Тіло відповіді не може перевищувати 1MB")
        String responseBody,

        @Min(value = 100, message = "Статус має бути >= 100")
        @Max(value = 599, message = "Статус має бути <= 599")
        Integer responseStatus,

        @Size(max = 100)
        String contentType,

        @Min(value = 0, message = "Затримка не може бути від'ємною")
        @Max(value = 30000, message = "Максимальна затримка - 30 секунд")
        Integer delayMs,

        Integer ttlHours

) {}
