package com.example.passwordgenerator.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordGenerationRequestTest {

    @Test
    public void testSettersAndGetters() {
        PasswordGenerationRequest request = new PasswordGenerationRequest();
        request.setLength(8);
        request.setComplexity(2);
        request.setOwner("user1");

        assertEquals(8, request.getLength());
        assertEquals(2, request.getComplexity());
        assertEquals("user1", request.getOwner());
    }

    @Test
    public void testConstructor() {
        PasswordGenerationRequest request = new PasswordGenerationRequest(8, 2, "user1");
        assertEquals(8, request.getLength());
        assertEquals(2, request.getComplexity());
        assertEquals("user1", request.getOwner());
    }
}