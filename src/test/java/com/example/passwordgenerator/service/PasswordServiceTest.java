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

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordService passwordService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordService = new PasswordService(passwordRepository, passwordCache, passwordEncoder);
    }

    @Test
    public void checkPasswordGenerationForNumbersOnly() {
        when(passwordCache.getGeneratedPassword("8_1_user1")).thenReturn(Optional.empty());
        String result = passwordService.generatePassword(8, 1, "user1");
        assertNotNull(result);
        assertEquals(8, result.length());
        assertTrue(result.chars().allMatch(Character::isDigit));
    }

    @Test
    public void testGeneratePasswordSuccessComplexity2() {
        when(passwordCache.getGeneratedPassword("8_2_user1")).thenReturn(Optional.empty());
        String password = passwordService.generatePassword(8, 2, "user1");
        assertNotNull(password);
        assertEquals(8, password.length());
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        assertTrue(hasLetter, "Пароль должен содержать хотя бы одну букву!");
    }

    @Test
    public void testGeneratePasswordSuccessComplexity30() {
        when(passwordCache.getGeneratedPassword("8_3_user1")).thenReturn(Optional.empty());
        String password = passwordService.generatePassword(8, 3, "user1");
        assertNotNull(password);
        assertEquals(8, password.length());
        boolean hasSymbol = password.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(c) >= 0);
        assertTrue(hasSymbol, "Пароль должен содержать хотя бы один символ!");
    }

    @Test
    public void testGeneratePasswordInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> passwordService.generatePassword(3, 2, "user1"));
    }

    @Test
    public void testGeneratePasswordInvalidComplexity() {
        assertThrows(IllegalArgumentException.class, () -> passwordService.generatePassword(8, 4, "user1"));
    }

    @Test
    public void testGeneratePasswordsBulkSuccess() {
        List<PasswordGenerationRequest> requests = Arrays.asList(
                new PasswordGenerationRequest(8, 2, "user1"),
                new PasswordGenerationRequest(10, 3, "user2")
        );
        when(passwordCache.getBulkPasswords(anyString())).thenReturn(Optional.empty());
        when(passwordRepository.save(any(Password.class))).thenAnswer(invocation -> invocation.getArgument(0));
        List<String> passwords = passwordService.generatePasswordsBulk(requests);
        assertEquals(2, passwords.size());
        assertEquals(8, passwords.get(0).length());
        assertEquals(10, passwords.get(1).length());
    }

    @Test
    public void testGeneratePasswordsBulkCache() {
        List<PasswordGenerationRequest> requests = Arrays.asList(
                new PasswordGenerationRequest(8, 2, "user1"),
                new PasswordGenerationRequest(10, 3, "user2")
        );
        String cacheKey = "8_2_user1|10_3_user2";
        when(passwordCache.getBulkPasswords(cacheKey)).thenReturn(Optional.empty());
        when(passwordRepository.save(any(Password.class))).thenAnswer(invocation -> invocation.getArgument(0));
        List<String> passwords = passwordService.generatePasswordsBulk(requests);
        verify(passwordCache).putBulkPasswords(cacheKey, passwords);
        when(passwordCache.getBulkPasswords(cacheKey)).thenReturn(Optional.of(passwords));
        List<String> cachedPasswords = passwordService.generatePasswordsBulk(requests);
        assertEquals(passwords, cachedPasswords);
    }

    @Test
    public void testUpdatePasswordNotFound() {
        Password password = new Password("newPass", "owner");
        password.setId(999L);
        when(passwordRepository.existsById(999L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> passwordService.update(password));
    }

    @Test
    public void testGeneratePasswordsBulkCacheHit() {
        List<PasswordGenerationRequest> requests = Arrays.asList(
                new PasswordGenerationRequest(8, 2, "user1")
        );
        when(passwordCache.getBulkPasswords(anyString())).thenReturn(Optional.of(Arrays.asList("cachedPass")));
        List<String> passwords = passwordService.generatePasswordsBulk(requests);
        assertEquals(1, passwords.size());
        assertEquals("cachedPass", passwords.get(0));
    }

    @Test
    public void testGeneratePasswordsEmptyRequest() {
        List<String> passwords = passwordService.generatePasswordsBulk(List.of());
        assertTrue(passwords.isEmpty());
    }

    @Test
    public void testGeneratePasswordsNullRequest() {
        List<String> passwords = passwordService.generatePasswordsBulk(null);
        assertTrue(passwords.isEmpty());
    }

    @Test
    public void testGeneratePasswordsBulkWithNullElement() {
        List<PasswordGenerationRequest> requests = Arrays.asList(
                new PasswordGenerationRequest(8, 2, "user1"),
                null,
                new PasswordGenerationRequest(10, 3, "user2")
        );
        when(passwordCache.getBulkPasswords(anyString())).thenReturn(Optional.empty());
        when(passwordRepository.save(any(Password.class))).thenAnswer(invocation -> invocation.getArgument(0));
        List<String> passwords = passwordService.generatePasswordsBulk(requests);
        assertEquals(2, passwords.size());
        assertEquals(8, passwords.get(0).length());
        assertEquals(10, passwords.get(1).length());
    }


    @Test
    public void testCreate() {
        Password password = new Password("pass1", "user1");
        when(passwordEncoder.encode("pass1")).thenReturn("encodedPass1");
        when(passwordRepository.save(any(Password.class))).thenAnswer(invocation -> {
            Password p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        Password created = passwordService.create(password);
        assertEquals("encodedPass1", created.getPassword());
        assertEquals("user1", created.getOwner());
        verify(passwordEncoder).encode("pass1");
    }

    @Test
    public void testUpdate() {
        Password password = new Password("updatedPass", "user1");
        password.setId(1L);
        when(passwordRepository.existsById(1L)).thenReturn(true);
        when(passwordEncoder.encode("updatedPass")).thenReturn("encodedUpdatedPass");
        when(passwordRepository.save(any(Password.class))).thenReturn(password);
        Password updated = passwordService.update(password);
        assertEquals("encodedUpdatedPass", updated.getPassword());
        assertEquals("user1", updated.getOwner());
        verify(passwordEncoder).encode("updatedPass");
    }

    @Test
    public void testDelete() {
        doNothing().when(passwordRepository).deleteById(1L);
        passwordService.delete(1L);
        verify(passwordRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testFindByIdNotFound() {
        when(passwordCache.getPasswordById(1L)).thenReturn(Optional.empty());
        when(passwordRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<Password> result = passwordService.findById(1L);
        assertFalse(result.isPresent());
    }

    @Test
    public void testFindAllEmpty() {
        when(passwordCache.getAllPasswords()).thenReturn(Optional.empty());
        when(passwordRepository.findAll()).thenReturn(List.of());
        List<Password> result = passwordService.findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindPasswordsByTagNameEmpty() {
        when(passwordCache.getPasswordsByTag("tag1")).thenReturn(Optional.empty());
        when(passwordRepository.findPasswordsByTagName("tag1")).thenReturn(List.of());
        List<Password> result = passwordService.findPasswordsByTagName("tag1");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGeneratePasswordsBulkCacheConsistency() {
        List<PasswordGenerationRequest> requests = Arrays.asList(new PasswordGenerationRequest(8, 2, "user1"));
        String cacheKey = "8_2_user1";
        when(passwordCache.getGeneratedPassword("8_2_user1")).thenReturn(Optional.empty());
        when(passwordCache.getBulkPasswords(cacheKey)).thenReturn(Optional.empty());
        when(passwordRepository.save(any(Password.class))).thenAnswer(invocation -> invocation.getArgument(0));
        List<String> firstCall = passwordService.generatePasswordsBulk(requests);
        assertNotNull(firstCall);
        assertFalse(firstCall.isEmpty());
        when(passwordCache.getBulkPasswords(cacheKey)).thenReturn(Optional.of(firstCall));
        List<String> secondCall = passwordService.generatePasswordsBulk(requests);
        assertEquals(firstCall, secondCall);
    }
}