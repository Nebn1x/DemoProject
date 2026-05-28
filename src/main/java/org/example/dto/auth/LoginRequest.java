package org.example.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Email не може бути порожнім")
        @Email(message = "Невалідний формат email")
        String email,

        @NotBlank(message = "Пароль не може бути порожнім")
        String password

) {}
