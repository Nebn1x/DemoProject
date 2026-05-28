package org.example.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Email не може бути порожнім")
        @Email(message = "Невалідний формат email")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Пароль не може бути порожнім")
        @Size(min = 8, max = 100, message = "Пароль має містити від 8 до 100 символів")
        String password

) {}
