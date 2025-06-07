package com.example.passwordgenerator.controller;

import com.example.passwordgenerator.dto.PasswordGenerationRequest;
import com.example.passwordgenerator.entity.Password;
import com.example.passwordgenerator.service.PasswordService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passwords")
public class PasswordController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordController.class);
    private final PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @GetMapping(value = "/generate", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    public ResponseEntity<String> generatePassword(
            @RequestParam int length,
            @RequestParam int complexity,
            @RequestParam String owner) {
        String password = passwordService.generatePassword(length, complexity, owner);
        Password passwordEntity = new Password(password, owner);
        passwordService.create(passwordEntity);
        return ResponseEntity.ok("✅ Пароль для " + owner + ": " + password);
    }

    @PostMapping(value = "/generate-bulk", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> generatePasswordsBulk(@RequestBody List<PasswordGenerationRequest> requests) {
        logger.debug("Received requests: {}", requests);
        List<String> passwords = passwordService.generatePasswordsBulk(requests);
        logger.debug("Returning passwords: {}", passwords);
        return ResponseEntity.ok(passwords);
    }

    @GetMapping
    public List<Password> getAll() {
        return passwordService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Password> getById(@PathVariable Long id) {
        return passwordService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Password create(@Valid @RequestBody Password password) {
        return passwordService.create(password);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Password> update(@PathVariable Long id, @Valid @RequestBody Password password) {
        password.setId(id);
        Password updated = passwordService.update(password);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        passwordService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-tag")
    public List<Password> getPasswordsByTagName(@RequestParam String tagName) {
        return passwordService.findPasswordsByTagName(tagName);
    }
}