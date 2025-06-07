package com.example.passwordgenerator.cache;

import com.example.passwordgenerator.entity.Password;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordCacheTest {

    @Test
    public void testPutAndGetGeneratedPassword() {
        PasswordCache cache = new PasswordCache();
        String cacheKey = "8_2_user1";
        cache.putGeneratedPassword(cacheKey, "testPass");
        Optional<String> result = cache.getGeneratedPassword(cacheKey);
        if (!result.isPresent()) {
            fail("Пароль должен быть в кэше!");
        }
        if (!"testPass".equals(result.get())) {
            fail("Ожидаемый пароль 'testPass', но получен '" + result.get() + "'");
        }
    }

    @Test
    public void testBulkPasswordsCache() {
        PasswordCache cache = new PasswordCache();
        List<String> passwords = List.of("pass1", "pass2", "pass3");
        String cacheKey = "key123";

        cache.putBulkPasswords(cacheKey, passwords);
        Optional<List<String>> result = cache.getBulkPasswords(cacheKey);

        assertTrue(result.isPresent());
        assertEquals(3, result.get().size());
        assertEquals("pass1", result.get().get(0));
    }

    @Test
    public void testBulkPasswordsCacheMiss() {
        PasswordCache cache = new PasswordCache();
        Optional<List<String>> result = cache.getBulkPasswords("missingKey");
        assertFalse(result.isPresent());
    }

    @Test
    public void testPutAndGetBulkPasswords() {
        PasswordCache cache = new PasswordCache();
        List<String> passwords = List.of("pass1", "pass2");
        cache.putBulkPasswords("key", passwords);
        Optional<List<String>> result = cache.getBulkPasswords("key");
        if (!result.isPresent()) {
            fail("Список паролей должен быть в кэше!");
        }
        if (!passwords.equals(result.get())) {
            fail("Ожидаемый список паролей не совпадает с полученным");
        }
    }

    @Test
    public void testPutAndGetPasswordById() {
        PasswordCache cache = new PasswordCache();
        Password password = new Password("pass1", "user1");
        cache.putPasswordById(1L, password);
        Optional<Password> result = cache.getPasswordById(1L);
        if (!result.isPresent()) {
            fail("Пароль должен быть в кэше!");
        }
        if (!password.equals(result.get())) {
            fail("Ожидаемый пароль не совпадает с полученным");
        }
    }

    @Test
    public void testPutAndGetAllPasswords() {
        PasswordCache cache = new PasswordCache();
        List<Password> passwords = List.of(new Password("pass1", "user1"));
        cache.putAllPasswords(passwords);
        Optional<List<Password>> result = cache.getAllPasswords();
        if (!result.isPresent()) {
            fail("Список паролей должен быть в кэше!");
        }
        if (!passwords.equals(result.get())) {
            fail("Ожидаемый список паролей не совпадает с полученным");
        }
    }

    @Test
    public void testPutAndGetPasswordsByTag() {
        PasswordCache cache = new PasswordCache();
        List<Password> passwords = List.of(new Password("pass1", "user1"));
        cache.putPasswordsByTag("tag1", passwords);
        Optional<List<Password>> result = cache.getPasswordsByTag("tag1");
        if (!result.isPresent()) {
            fail("Список паролей для тега должен быть в кэше!");
        }
        if (!passwords.equals(result.get())) {
            fail("Ожидаемый список паролей для тега не совпадает с полученным");
        }
    }

    @Test
    public void testClearDatabaseCache() {
        PasswordCache cache = new PasswordCache();
        cache.putPasswordById(1L, new Password("pass1", "user1"));
        cache.clearDatabaseCache();
        Optional<Password> result = cache.getPasswordById(1L);
        if (result.isPresent()) {
            fail("Кэ должен быть очищен!");
        }
    }

    @Test
    public void testGetGeneratedPasswordMiss() {
        PasswordCache cache = new PasswordCache();
        String cacheKey = "8_2_user1";
        Optional<String> result = cache.getGeneratedPassword(cacheKey);
        if (result.isPresent()) {
            fail("Пароль не должен быть в кэше!");
        }
    }

    @Test
    public void testGetBulkPasswordsMiss() {
        PasswordCache cache = new PasswordCache();
        Optional<List<String>> result = cache.getBulkPasswords("key");
        if (result.isPresent()) {
            fail("Список паролей не должен быть в кэше!");
        }
    }
}