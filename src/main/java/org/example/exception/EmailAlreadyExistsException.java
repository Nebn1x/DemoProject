package org.example.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("Користувач з email '" + email + "' вже існує");
    }
}