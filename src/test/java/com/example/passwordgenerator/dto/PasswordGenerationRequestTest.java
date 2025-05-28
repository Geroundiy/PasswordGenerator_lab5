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
        if (request.getLength() != 8) {
            fail("Ожидаемая длина 8, но получено " + request.getLength());
        }
        if (request.getComplexity() != 2) {
            fail("Ожидаемая сложность 2, но получено " + request.getComplexity());
        }
        if (!"user1".equals(request.getOwner())) {
            fail("Ожидаемый владелец 'user1', но получен '" + request.getOwner() + "'");
        }
    }

    @Test
    public void testConstructor() {
        PasswordGenerationRequest request = new PasswordGenerationRequest(8, 2, "user1");
        if (request.getLength() != 8) {
            fail("Ожидаемая длина 8, но получено " + request.getLength());
        }
        if (request.getComplexity() != 2) {
            fail("Ожидаемая сложность 2, но получено " + request.getComplexity());
        }
        if (!"user1".equals(request.getOwner())) {
            fail("Ожидаемый владелец 'user1', но получен '" + request.getOwner() + "'");
        }
    }
}