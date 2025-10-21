package com.example.safetynetalerts.exeption;

import com.example.safetynetalerts.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleIllegalArgumentException_returnsBadRequestAndMessage() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input data");

        ResponseEntity<Map<String, String>> response = handler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid input data", response.getBody().get("message"));
    }

    @Test
    void handleGenericException_returnsInternalServerErrorAndMessage() {
        Exception ex = new Exception("Something broke internally");

        ResponseEntity<Map<String, String>> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message")
                .contains("An unexpected error occurred: Something broke internally"));
    }

}
