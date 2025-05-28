package com.example.passwordgenerator.service;

import com.example.passwordgenerator.cache.PasswordCache;
import com.example.passwordgenerator.dto.PasswordGenerationRequest;
import com.example.passwordgenerator.entity.Password;
import com.example.passwordgenerator.repository.PasswordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    private PasswordService passwordService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordService = new PasswordService(passwordRepository, passwordCache);
    }

    @Test
    public void checkPasswordGenerationForNumbersOnly() {
        when(passwordCache.getGeneratedPassword(8, 1)).thenReturn(Optional.empty());
        String result = passwordService.generatePassword(8, 1);
        if (result == null) {
            fail("Пароль не должен быть null!");
        }
        int expectedLength = 8;
        if (result.length() != expectedLength) {
            fail("Ожидаемая длина пароля " + expectedLength + ", но получили " + result.length());
        }
        for (char c : result.toCharArray()) {
            if (!Character.isDigit(c)) {
                fail("Пароль должен содержать только цифры, но найден символ: " + c);
            }
        }
    }

    @Test
    public void testGeneratePasswordSuccessComplexity2() {
        when(passwordCache.getGeneratedPassword(8, 2)).thenReturn(Optional.empty());
        String password = passwordService.generatePassword(8, 2);
        if (password == null) {
            fail("Пароль не должен быть null!");
        }
        if (password.length() != 8) {
            fail("Ожидаемая длина пароля 8, но получили " + password.length());
        }
        boolean hasLetter = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
                break;
            }
        }
        if (!hasLetter) {
            fail("Пароль должен содержать хотя бы одну букву!");
        }
    }

    @Test
    public void testGeneratePasswordSuccessComplexity3() {
        when(passwordCache.getGeneratedPassword(8, 3)).thenReturn(Optional.empty());
        String password = passwordService.generatePassword(8, 3);
        if (password == null) {
            fail("Пароль не должен быть null!");
        }
        if (password.length() != 8) {
            fail("Ожидаемая длина пароля 8, но получили " + password.length());
        }
        boolean hasSymbol = false;
        for (char c : password.toCharArray()) {
            if ("!@#$%^&*()_-+=<>?/{}[]|".indexOf(c) != -1) {
                hasSymbol = true;
                break;
            }
        }
        if (!hasSymbol) {
            fail("Пароль должен содержать хотя бы один символ!");
        }
    }

    @Test
    public void testGeneratePasswordCacheHit() {
        when(passwordCache.getGeneratedPassword(8, 2)).thenReturn(Optional.of("cachedPass"));
        String password = passwordService.generatePassword(8, 2);
        if (!"cachedPass".equals(password)) {
            fail("Ожидаемый пароль 'cachedPass', но получен '" + password + "'");
        }
    }

    @Test
    public void testGeneratePasswordInvalidLength() {
        try {
            passwordService.generatePassword(3, 2);
            fail("Должно быть выброшено исключение для неверной длины!");
        } catch (IllegalArgumentException e) {
            if (!"Длина пароля должна быть от 4 до 30 символов.".equals(e.getMessage())) {
                fail("Неверное сообщение об ошибке: " + e.getMessage());
            }
        }
    }

    @Test
    public void testGeneratePasswordInvalidComplexity() {
        try {
            passwordService.generatePassword(8, 4);
            fail("Должно быть выброшено исключение для неверной сложности!");
        } catch (IllegalArgumentException e) {
            if (!"Уровень сложности должен быть от 1 до 3.".equals(e.getMessage())) {
                fail("Неверное сообщение об ошибке: " + e.getMessage());
            }
        }
    }

    @Test
    public void testGeneratePasswordsBulkSuccess() {
        List<PasswordGenerationRequest> requests = List.of(
                new PasswordGenerationRequest(8, 2, "user1"),
                new PasswordGenerationRequest(10, 3, "user2")
        );
        when(passwordCache.getBulkPasswords(anyString())).thenReturn(Optional.empty());
        when(passwordRepository.save(any(Password.class))).thenAnswer(invocation -> invocation.getArgument(0));
        List<String> passwords = passwordService.generatePasswordsBulk(requests);
        if (passwords.size() != 2) {
            fail("Ожидается 2 пароля, но получено " + passwords.size());
        }
        if (passwords.get(0).length() != 8) {
            fail("Длина первого пароля должна быть 8, но " + passwords.get(0).length());
        }
        if (passwords.get(1).length() != 10) {
            fail("Длина второго пароля должна быть 10, но " + passwords.get(1).length());
        }
    }

    @Test
    public void testGeneratePasswordsBulkCacheHit() {
        List<PasswordGenerationRequest> requests = List.of(
                new PasswordGenerationRequest(8, 2, "user1")
        );
        when(passwordCache.getBulkPasswords(anyString())).thenReturn(Optional.of(List.of("cachedPass")));
        List<String> passwords = passwordService.generatePasswordsBulk(requests);
        if (passwords.size() != 1) {
            fail("Ожидается 1 пароль, но получено " + passwords.size());
        }
        if (!"cachedPass".equals(passwords.get(0))) {
            fail("Ожидаемый пароль 'cachedPass', но получен '" + passwords.get(0) + "'");
        }
    }

    @Test
    public void testGeneratePasswordsBulkEmptyRequest() {
        List<String> passwords = passwordService.generatePasswordsBulk(List.of());
        if (!passwords.isEmpty()) {
            fail("Ожидается пустой список, но получено " + passwords.size() + " элементов");
        }
    }

    @Test
    public void testGeneratePasswordsBulkNullRequest() {
        List<String> passwords = passwordService.generatePasswordsBulk(null);
        if (!passwords.isEmpty()) {
            fail("Ожидается пустой список, но получено " + passwords.size() + " элементов");
        }
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
        if (passwords.size() != 2) {
            fail("Ожидается 2 пароля, но получено " + passwords.size());
        }
        if (passwords.get(0).length() != 8) {
            fail("Длина первого пароля должна быть 8, но " + passwords.get(0).length());
        }
        if (passwords.get(1).length() != 10) {
            fail("Длина второго пароля должна быть 10, но " + passwords.get(1).length());
        }
    }

    @Test
    public void testCreate() {
        Password password = new Password("pass1", "user1");
        when(passwordRepository.save(any(Password.class))).thenAnswer(invocation -> {
            Password p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        Password created = passwordService.create(password);
        if (!passwordEncoder.matches("pass1", created.getPassword())) {
            fail("Пароль не зашифрован!");
        }
        if (!"user1".equals(created.getOwner())) {
            fail("Неверный владелец: " + created.getOwner());
        }
    }

    @Test
    public void testUpdate() {
        Password password = new Password("updatedPass", "user1");
        password.setId(1L);
        when(passwordRepository.save(any(Password.class))).thenReturn(password);
        Password updated = passwordService.update(password);
        if (!passwordEncoder.matches("updatedPass", updated.getPassword())) {
            fail("Пароль не обновлен!");
        }
        if (!"user1".equals(updated.getOwner())) {
            fail("Неверный владелец: " + updated.getOwner());
        }
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
        if (result.isPresent()) {
            fail("Не ожидается нахождение пароля!");
        }
    }

    @Test
    public void testFindAllEmpty() {
        when(passwordCache.getAllPasswords()).thenReturn(Optional.empty());
        when(passwordRepository.findAll()).thenReturn(List.of());
        List<Password> result = passwordService.findAll();
        if (!result.isEmpty()) {
            fail("Ожидается пустой список, но получено " + result.size() + " элементов");
        }
    }

    @Test
    public void testFindPasswordsByTagNameEmpty() {
        when(passwordCache.getPasswordsByTag("tag1")).thenReturn(Optional.empty());
        when(passwordRepository.findPasswordsByTagName("tag1")).thenReturn(List.of());
        List<Password> result = passwordService.findPasswordsByTagName("tag1");
        if (!result.isEmpty()) {
            fail("Ожидается пустой список, но получено " + result.size() + " элементов");
        }
    }
}