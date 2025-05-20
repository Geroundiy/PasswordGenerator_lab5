package com.example.passwordgenerator.controller;

import com.example.passwordgenerator.dto.PasswordGenerationRequest;
import com.example.passwordgenerator.entity.Password;
import com.example.passwordgenerator.service.PasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления паролями.
 * Предоставляет эндпоинты для генерации, получения, создания, обновления и удаления паролей.
 */
@RestController
@RequestMapping("/api/passwords")
public class PasswordController {

    private final PasswordService passwordService;

    /**
     * Конструктор контроллера.
     *
     * @param passwordService сервис для работы с паролями
     */
    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    /**
     * Генерирует пароль на основе указанных параметров и сохраняет его в базе данных.
     *
     * @param length     длина пароля (от 4 до 30 символов)
     * @param complexity уровень сложности пароля (от 1 до 3)
     * @param owner      владелец пароля
     * @return ответ с сгенерированным паролем
     * @throws IllegalArgumentException если параметры не соответствуют допустимым значениям
     */
    @GetMapping("/generate")
    public ResponseEntity<String> generatePassword(
            @RequestParam int length,
            @RequestParam int complexity,
            @RequestParam String owner) {

        if (length < 4 || length > 30) {
            throw new IllegalArgumentException("Длина пароля должна быть от 4 до 30 символов.");
        }
        if (complexity < 1 || complexity > 3) {
            throw new IllegalArgumentException("Уровень сложности должен быть от 1 до 3.");
        }

        String password = passwordService.generatePassword(length, complexity);
        Password passwordEntity = new Password(password, owner);
        passwordService.create(passwordEntity);

        return ResponseEntity.ok("✅ Пароль для " + owner + ": " + password);
    }

    /**
     * Генерирует несколько паролей на основе списка параметров.
     *
     * @param requests список параметров для генерации паролей
     * @return список сгенерированных паролей
     */
    @PostMapping("/generate-bulk")
    public ResponseEntity<List<String>> generatePasswordsBulk(@RequestBody List<PasswordGenerationRequest> requests) {
        List<String> passwords = passwordService.generatePasswordsBulk(requests);
        return ResponseEntity.ok(passwords);
    }

    /**
     * Возвращает список всех паролей.
     *
     * @return список паролей
     */
    @GetMapping
    public List<Password> getAll() {
        return passwordService.findAll();
    }

    /**
     * Возвращает пароль по его ID.
     *
     * @param id ID пароля
     * @return ответ с паролем или 404, если пароль не найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<Password> getById(@PathVariable Long id) {
        return passwordService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Создает новый пароль.
     *
     * @param password объект пароля для создания
     * @return созданный пароль
     */
    @PostMapping
    public Password create(@RequestBody Password password) {
        return passwordService.create(password);
    }

    /**
     * Обновляет существующий пароль.
     *
     * @param id       ID пароля для обновления
     * @param password объект пароля с новыми данными
     * @return ответ с обновленным паролем
     */
    @PutMapping("/{id}")
    public ResponseEntity<Password> update(@PathVariable Long id, @RequestBody Password password) {
        password.setId(id);
        Password updated = passwordService.update(password);
        return ResponseEntity.ok(updated);
    }

    /**
     * Удаляет пароль по его ID.
     *
     * @param id ID пароля для удаления
     * @return ответ без содержимого
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        passwordService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Возвращает список паролей, связанных с указанным тегом.
     *
     * @param tagName имя тега
     * @return список паролей
     */
    @GetMapping("/by-tag")
    public List<Password> getPasswordsByTagName(@RequestParam String tagName) {
        return passwordService.findPasswordsByTagName(tagName);
    }
}