package com.example.passwordgenerator.service;

import com.example.passwordgenerator.cache.PasswordCache;
import com.example.passwordgenerator.dto.PasswordGenerationRequest;
import com.example.passwordgenerator.entity.Password;
import com.example.passwordgenerator.repository.PasswordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PasswordServiceTest {

    @Mock
    private PasswordRepository passwordRepository;

    @Mock
    private PasswordCache passwordCache;

    @InjectMocks
    private PasswordService passwordService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();
        passwordService = new PasswordService(passwordRepository, passwordCache);
    }

    @Test
    public void testGeneratePassword() {
        when(passwordCache.getGeneratedPassword(10, 2)).thenReturn(Optional.empty());
        String password = passwordService.generatePassword(10, 2);
        assertEquals(10, password.length());
        verify(passwordCache).putGeneratedPassword(10, 2, password);
    }

    @Test
    public void testGeneratePasswordFromCache() {
        when(passwordCache.getGeneratedPassword(10, 2)).thenReturn(Optional.of("cachedPass"));
        String password = passwordService.generatePassword(10, 2);
        assertEquals("cachedPass", password);
        verify(passwordCache, never()).putGeneratedPassword(anyInt(), anyInt(), anyString());
    }

    @Test
    public void testCreate() {
        Password password = new Password("plainPass", "user1");
        when(passwordRepository.save(any(Password.class))).thenAnswer(invocation -> {
            Password p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        Password saved = passwordService.create(password);
        assertNotEquals("plainPass", saved.getPassword());
        assertTrue(passwordEncoder.matches("plainPass", saved.getPassword()));
        assertEquals("user1", saved.getOwner());
        verify(passwordCache).clearDatabaseCache();
    }

    @Test
    public void testFindAll() {
        List<Password> passwords = Arrays.asList(new Password("pass1", "user1"), new Password("pass2", "user2"));
        when(passwordCache.getAllPasswords()).thenReturn(Optional.empty());
        when(passwordRepository.findAll()).thenReturn(passwords);
        List<Password> result = passwordService.findAll();
        assertEquals(2, result.size());
        verify(passwordCache).putAllPasswords(passwords);
    }

    @Test
    public void testFindById() {
        Password password = new Password("pass1", "user1");
        when(passwordCache.getPasswordById(1L)).thenReturn(Optional.empty());
        when(passwordRepository.findById(1L)).thenReturn(Optional.of(password));
        Optional<Password> result = passwordService.findById(1L);
        assertTrue(result.isPresent());
        assertEquals("pass1", result.get().getPassword());
        verify(passwordCache).putPasswordById(1L, password);
    }

    @Test
    public void testUpdate() {
        Password password = new Password("newPass", "user1");
        password.setId(1L);
        when(passwordRepository.save(any(Password.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Password updated = passwordService.update(password);
        assertNotEquals("newPass", updated.getPassword());
        assertTrue(passwordEncoder.matches("newPass", updated.getPassword()));
        assertEquals(1L, updated.getId());
        verify(passwordCache).clearDatabaseCache();
    }

    @Test
    public void testDelete() {
        doNothing().when(passwordRepository).deleteById(1L);
        passwordService.delete(1L);
        verify(passwordRepository).deleteById(1L);
        verify(passwordCache).clearDatabaseCache();
    }

    @Test
    public void testFindPasswordsByTagName() {
        List<Password> passwords = Arrays.asList(new Password("pass1", "user1"));
        when(passwordCache.getPasswordsByTag("tag1")).thenReturn(Optional.empty());
        when(passwordRepository.findPasswordsByTagName("tag1")).thenReturn(passwords);
        List<Password> result = passwordService.findPasswordsByTagName("tag1");
        assertEquals(1, result.size());
        verify(passwordCache).putPasswordsByTag("tag1", passwords);
    }

    @Test
    public void testGeneratePasswordsBulk() {
        List<PasswordGenerationRequest> requests = Arrays.asList(
                new PasswordGenerationRequest(8, 2, "user1"),
                new PasswordGenerationRequest(10, 3, "user2")
        );
        String cacheKey = "8_2_user1|10_3_user2";

        when(passwordCache.getBulkPasswords(cacheKey)).thenReturn(Optional.empty());
        when(passwordRepository.save(any(Password.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<String> passwords = passwordService.generatePasswordsBulk(requests);

        assertEquals(2, passwords.size());
        assertEquals(8, passwords.get(0).length());
        assertEquals(10, passwords.get(1).length());
        verify(passwordCache).putBulkPasswords(cacheKey, passwords);
        verify(passwordCache).clearDatabaseCache();
    }

    @Test
    public void testGeneratePasswordsBulkFromCache() {
        List<PasswordGenerationRequest> requests = Arrays.asList(
                new PasswordGenerationRequest(8, 2, "user1"),
                new PasswordGenerationRequest(10, 3, "user2")
        );
        String cacheKey = "8_2_user1|10_3_user2";
        List<String> cachedPasswords = Arrays.asList("password1", "password12");

        when(passwordCache.getBulkPasswords(cacheKey)).thenReturn(Optional.of(cachedPasswords));

        List<String> passwords = passwordService.generatePasswordsBulk(requests);

        assertEquals(2, passwords.size());
        assertEquals("password1", passwords.get(0));
        assertEquals("password12", passwords.get(1));
        verify(passwordCache, never()).putBulkPasswords(anyString(), anyList());
        verify(passwordRepository, never()).save(any(Password.class));
    }

    @Test
    public void testGeneratePasswordsBulkInvalidLength() {
        List<PasswordGenerationRequest> requests = Arrays.asList(
                new PasswordGenerationRequest(3, 2, "user1")
        );
        assertThrows(IllegalArgumentException.class, () -> passwordService.generatePasswordsBulk(requests));
    }

    @Test
    public void testGeneratePasswordsBulkInvalidComplexity() {
        List<PasswordGenerationRequest> requests = Arrays.asList(
                new PasswordGenerationRequest(8, 4, "user1")
        );
        assertThrows(IllegalArgumentException.class, () -> passwordService.generatePasswordsBulk(requests));
    }

    @Test
    public void testCreateBulk() {
        List<Password> passwords = Arrays.asList(
                new Password("pass1", "user1"),
                new Password("pass2", "user2")
        );
        when(passwordRepository.save(any(Password.class))).thenAnswer(invocation -> {
            Password p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        List<Password> saved = passwordService.createBulk(passwords);
        assertEquals(2, saved.size());
        assertNotEquals("pass1", saved.get(0).getPassword());
        assertNotEquals("pass2", saved.get(1).getPassword());
        assertTrue(passwordEncoder.matches("pass1", saved.get(0).getPassword()));
        assertTrue(passwordEncoder.matches("pass2", saved.get(1).getPassword()));
        verify(passwordCache).clearDatabaseCache();
    }
}