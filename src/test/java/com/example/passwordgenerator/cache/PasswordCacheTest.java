package com.example.passwordgenerator.cache;

import com.example.passwordgenerator.entity.Password;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordCacheTest {

    @Mock
    private PasswordCache cache;

    @BeforeEach
    public void setUp() {
        when(cache.getGeneratedPassword("8_2_user1")).thenReturn(Optional.of("testPass"));
        when(cache.getBulkPasswords("key")).thenReturn(Optional.of(List.of("pass1", "pass2")));
        when(cache.getBulkPasswords("key123")).thenReturn(Optional.of(List.of("pass1", "pass2", "pass3")));
        when(cache.getBulkPasswords("missingKey")).thenReturn(Optional.empty());
        when(cache.getPasswordById(1L)).thenReturn(Optional.of(new Password("pass1", "user1")));
        when(cache.getAllPasswords()).thenReturn(Optional.of(List.of(new Password("pass1", "user1"))));
        when(cache.getPasswordsByTag("tag1")).thenReturn(Optional.of(List.of(new Password("pass1", "user1"))));
        doNothing().when(cache).putGeneratedPassword(anyString(), anyString());
        doNothing().when(cache).putBulkPasswords(anyString(), anyList());
        doNothing().when(cache).putPasswordById(anyLong(), any(Password.class));
        doNothing().when(cache).putAllPasswords(anyList());
        doNothing().when(cache).putPasswordsByTag(anyString(), anyList());
        doNothing().when(cache).clearDatabaseCache();
    }

    @Test
    public void testPutAndGetGeneratedPassword() {
        cache.putGeneratedPassword("8_2_user1", "testPass");
        Optional<String> result = cache.getGeneratedPassword("8_2_user1");
        assertTrue(result.isPresent());
        assertEquals("testPass", result.get());
    }

    @Test
    public void testBulkPasswordsCache() {
        cache.putBulkPasswords("key123", List.of("pass1", "pass2", "pass3"));
        Optional<List<String>> result = cache.getBulkPasswords("key123");
        assertTrue(result.isPresent());
        assertEquals(3, result.get().size());
        assertEquals("pass1", result.get().get(0));
    }

    @Test
    public void testBulkPasswordsCacheMiss() {
        Optional<List<String>> result = cache.getBulkPasswords("missingKey");
        assertFalse(result.isPresent());
    }

    @Test
    public void testPutAndGetBulkPasswords() {
        cache.putBulkPasswords("key", List.of("pass1", "pass2"));
        Optional<List<String>> result = cache.getBulkPasswords("key");
        assertTrue(result.isPresent());
        assertEquals(List.of("pass1", "pass2"), result.get());
    }

    @Test
    public void testPutAndGetPasswordById() {
        Password password = new Password("pass1", "user1");
        cache.putPasswordById(1L, password);
        Optional<Password> result = cache.getPasswordById(1L);
        assertTrue(result.isPresent());
        assertEquals(password, result.get());
    }

    @Test
    public void testPutAndGetAllPasswords() {
        List<Password> passwords = List.of(new Password("pass1", "user1"));
        cache.putAllPasswords(passwords);
        Optional<List<Password>> result = cache.getAllPasswords();
        assertTrue(result.isPresent());
        assertEquals(passwords, result.get());
    }

    @Test
    public void testPutAndGetPasswordsByTag() {
        List<Password> passwords = List.of(new Password("pass1", "user1"));
        cache.putPasswordsByTag("tag1", passwords);
        Optional<List<Password>> result = cache.getPasswordsByTag("tag1");
        assertTrue(result.isPresent());
        assertEquals(passwords, result.get());
    }

    @Test
    public void testClearDatabaseCache() {
        cache.putPasswordById(1L, new Password("pass1", "user1"));
        cache.clearDatabaseCache();
        Optional<Password> result = cache.getPasswordById(1L);
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetGeneratedPasswordMiss() {
        Optional<String> result = cache.getGeneratedPassword("8_2_user1");
        assertTrue(result.isPresent());
    }

    @Test
    public void testGetBulkPasswordsMiss() {
        Optional<List<String>> result = cache.getBulkPasswords("key");
        assertTrue(result.isPresent());
    }
}