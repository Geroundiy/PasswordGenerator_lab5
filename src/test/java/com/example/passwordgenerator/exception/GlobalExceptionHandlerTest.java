package com.example.passwordgenerator.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    @Test
    public void testHandleIllegalArgumentException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        IllegalArgumentException exception = new IllegalArgumentException("Test error");
        ResponseEntity<String> response = handler.handleIllegalArgumentException(exception);
        if (response.getStatusCode() != HttpStatus.BAD_REQUEST) {
            fail("Ожидаемый статус BAD_REQUEST, но получен " + response.getStatusCode());
        }
        if (!"Test error".equals(response.getBody())) {
            fail("Ожидаемое тело ответа 'Test error', но получено '" + response.getBody() + "'");
        }
    }
}