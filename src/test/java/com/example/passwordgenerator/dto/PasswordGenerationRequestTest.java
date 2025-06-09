package com.example.passwordgenerator.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordGenerationRequestTest {

    @Mock
    private PasswordGenerationRequest request;

    @BeforeEach
    public void setUp() {
        when(request.getLength()).thenReturn(8);
        when(request.getComplexity()).thenReturn(2);
        when(request.getOwner()).thenReturn("user1");
        doNothing().when(request).setLength(anyInt());
        doNothing().when(request).setComplexity(anyInt());
        doNothing().when(request).setOwner(anyString());
    }

    @Test
    public void testSettersAndGetters() {
        request.setLength(8);
        request.setComplexity(2);
        request.setOwner("user1");
        assertEquals(8, request.getLength());
        assertEquals(2, request.getComplexity());
        assertEquals("user1", request.getOwner());
    }

    @Test
    public void testConstructor() {
        PasswordGenerationRequest requestWithConstructor = mock(PasswordGenerationRequest.class);
        when(requestWithConstructor.getLength()).thenReturn(8);
        when(requestWithConstructor.getComplexity()).thenReturn(2);
        when(requestWithConstructor.getOwner()).thenReturn("user1");
        assertEquals(8, requestWithConstructor.getLength());
        assertEquals(2, requestWithConstructor.getComplexity());
        assertEquals("user1", requestWithConstructor.getOwner());
    }
}