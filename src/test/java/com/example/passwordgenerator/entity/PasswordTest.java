package com.example.passwordgenerator.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordTest {

    @Test
    public void testPasswordSettersAndGetters() {
        Password password = new Password();
        password.setId(1L);
        password.setPassword("pass1");
        password.setOwner("user1");
        if (password.getId() != 1L) {
            fail("Ожидаемый ID 1, но получено " + password.getId());
        }
        if (!"pass1".equals(password.getPassword())) {
            fail("Ожидаемый пароль 'pass1', но получен '" + password.getPassword() + "'");
        }
        if (!"user1".equals(password.getOwner())) {
            fail("Ожидаемый владелец 'user1', но получен '" + password.getOwner() + "'");
        }
    }

    @Test
    public void testPasswordConstructor() {
        Password password = new Password("pass1", "user1");
        if (!"pass1".equals(password.getPassword())) {
            fail("Ожидаемый пароль 'pass1', но получен '" + password.getPassword() + "'");
        }
        if (!"user1".equals(password.getOwner())) {
            fail("Ожидаемый владелец 'user1', но получен '" + password.getOwner() + "'");
        }
    }
}