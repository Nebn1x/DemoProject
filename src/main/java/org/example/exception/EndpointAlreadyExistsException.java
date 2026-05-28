package org.example.exception;

public class EndpointAlreadyExistsException extends RuntimeException {
    public EndpointAlreadyExistsException(String method, String path) {
        super("Ендпоінт " + method + " " + path + " вже існує");
    }
}