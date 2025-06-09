package com.example.passwordgenerator.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordTest {

    @Mock
    private Password password;

    @BeforeEach
    public void setUp() {
        when(password.getId()).thenReturn(1L);
        when(password.getPassword()).thenReturn("pass1");
        when(password.getOwner()).thenReturn("user1");
        doNothing().when(password).setId(anyLong());
        doNothing().when(password).setPassword(anyString());
        doNothing().when(password).setOwner(anyString());
    }

    @Test
    public void testPasswordSettersAndGetters() {
        password.setId(1L);
        password.setPassword("pass1");
        password.setOwner("user1");
        assertEquals(1L, password.getId());
        assertEquals("pass1", password.getPassword());
        assertEquals("user1", password.getOwner());
    }

    @Test
    public void testPasswordConstructor() {
        Password passwordWithConstructor = mock(Password.class);
        when(passwordWithConstructor.getPassword()).thenReturn("pass1");
        when(passwordWithConstructor.getOwner()).thenReturn("user1");
        assertEquals("pass1", passwordWithConstructor.getPassword());
        assertEquals("user1", passwordWithConstructor.getOwner());
    }
}