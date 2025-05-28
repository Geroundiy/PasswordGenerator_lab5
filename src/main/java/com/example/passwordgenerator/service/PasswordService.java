package com.example.passwordgenerator.service;

import com.example.passwordgenerator.cache.PasswordCache;
import com.example.passwordgenerator.dto.PasswordGenerationRequest;
import com.example.passwordgenerator.entity.Password;
import com.example.passwordgenerator.repository.PasswordRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PasswordService {

    private static final String NUMBERS = "0123456789";
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String SYMBOLS = "!@#$%^&*()_-+=<>?/{}[]|";
    private final PasswordRepository passwordRepository;
    private final PasswordCache passwordCache;
    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordService(PasswordRepository passwordRepository, PasswordCache passwordCache) {
        this.passwordRepository = passwordRepository;
        this.passwordCache = passwordCache;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String generatePassword(int length, int complexity) {
        if (length < 4 || length > 30) {
            throw new IllegalArgumentException("Длина пароля должна быть от 4 до 30 символов.");
        }
        if (complexity < 1 || complexity > 3) {
            throw new IllegalArgumentException("Уровень сложности должен быть от 1 до 3.");
        }

        Optional<String> cachedPassword = passwordCache.getGeneratedPassword(length, complexity);
        if (cachedPassword.isPresent()) {
            return cachedPassword.get();
        }

        String characters = NUMBERS;
        if (complexity >= 2) {
            characters += LETTERS;
        }
        if (complexity >= 3) {
            characters += SYMBOLS;
        }

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        int guaranteedChars = 0;
        if (complexity >= 1 && length > guaranteedChars) {
            int index = random.nextInt(NUMBERS.length());
            password.append(NUMBERS.charAt(index));
            guaranteedChars++;
        }
        if (complexity >= 2 && length > guaranteedChars) {
            int index = random.nextInt(LETTERS.length());
            password.append(LETTERS.charAt(index));
            guaranteedChars++;
        }
        if (complexity >= 3 && length > guaranteedChars) {
            int index = random.nextInt(SYMBOLS.length());
            password.append(SYMBOLS.charAt(index));
            guaranteedChars++;
        }

        for (int i = guaranteedChars; i < length; i++) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));
        }

        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        String generatedPassword = new String(passwordArray);
        passwordCache.putGeneratedPassword(length, complexity, generatedPassword);
        return generatedPassword;
    }

    public List<String> generatePasswordsBulk(List<PasswordGenerationRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        String cacheKey = requests.stream()
                .filter(Objects::nonNull)
                .map(req -> req.getLength() + "_" + req.getComplexity() + "_" + req.getOwner())
                .collect(Collectors.joining("|"));

        Optional<List<String>> cachedPasswords = passwordCache.getBulkPasswords(cacheKey);
        if (cachedPasswords.isPresent()) {
            return cachedPasswords.get();
        }

        List<String> generatedPasswords = requests.stream()
                .filter(Objects::nonNull)
                .map(request -> generatePassword(request.getLength(), request.getComplexity()))
                .collect(Collectors.toList());

        passwordCache.putBulkPasswords(cacheKey, generatedPasswords);

        List<Password> passwordsToSave = new ArrayList<>();
        int passwordIndex = 0;
        for (PasswordGenerationRequest request : requests) {
            if (request != null) {
                passwordsToSave.add(new Password(generatedPasswords.get(passwordIndex), request.getOwner()));
                passwordIndex++;
            }
        }

        createBulk(passwordsToSave);
        return generatedPasswords;
    }

    public Password create(Password password) {
        String plainPassword = password.getPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);
        password.setPassword(hashedPassword);
        Password saved = passwordRepository.save(password);
        passwordCache.clearDatabaseCache();
        return saved;
    }

    public List<Password> findAll() {
        Optional<List<Password>> cachedPasswords = passwordCache.getAllPasswords();
        if (cachedPasswords.isPresent()) {
            return cachedPasswords.get();
        }
        List<Password> passwords = passwordRepository.findAll();
        passwordCache.putAllPasswords(passwords);
        return passwords;
    }

    public List<Password> createBulk(List<Password> passwords) {
        List<Password> savedPasswords = passwords.stream()
                .map(password -> {
                    String plainPassword = password.getPassword();
                    String hashedPassword = passwordEncoder.encode(plainPassword);
                    password.setPassword(hashedPassword);
                    return passwordRepository.save(password);
                })
                .collect(Collectors.toList());
        passwordCache.clearDatabaseCache();
        return savedPasswords;
    }

    public Optional<Password> findById(Long id) {
        Optional<Password> cachedPassword = passwordCache.getPasswordById(id);
        if (cachedPassword.isPresent()) {
            return cachedPassword;
        }
        Optional<Password> password = passwordRepository.findById(id);
        password.ifPresent(p -> passwordCache.putPasswordById(id, p));
        return password;
    }

    public Password update(Password password) {
        String plainPassword = password.getPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);
        password.setPassword(hashedPassword);
        Password saved = passwordRepository.save(password);
        passwordCache.clearDatabaseCache();
        return saved;
    }

    public void delete(Long id) {
        passwordRepository.deleteById(id);
        passwordCache.clearDatabaseCache();
    }

    public List<Password> findPasswordsByTagName(String tagName) {
        Optional<List<Password>> cachedPasswords = passwordCache.getPasswordsByTag(tagName);
        if (cachedPasswords.isPresent()) {
            return cachedPasswords.get();
        }
        List<Password> passwords = passwordRepository.findPasswordsByTagName(tagName);
        passwordCache.putPasswordsByTag(tagName, passwords);
        return passwords;
    }
}