package com.example.passwordgenerator.controller;

import com.example.passwordgenerator.dto.PasswordGenerationRequest;
import com.example.passwordgenerator.entity.Password;
import com.example.passwordgenerator.exception.GlobalExceptionHandler;
import com.example.passwordgenerator.service.PasswordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PasswordControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PasswordService passwordService;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        PasswordController passwordController = new PasswordController(passwordService);
        mockMvc = MockMvcBuilders.standaloneSetup(passwordController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testGeneratePassword() throws Exception {
        when(passwordService.generatePassword(8, 2)).thenReturn("password123");
        when(passwordService.create(any(Password.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(get("/api/passwords/generate")
                        .param("length", "8")
                        .param("complexity", "2")
                        .param("owner", "user1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("✅ Пароль для user1: password123"));
    }

    @Test
    public void testGeneratePasswordInvalidLength() throws Exception {
        when(passwordService.generatePassword(3, 2)).thenThrow(new IllegalArgumentException("Длина пароля должна быть от 4 до 30 символов."));
        mockMvc.perform(get("/api/passwords/generate")
                        .param("length", "3")
                        .param("complexity", "2")
                        .param("owner", "user1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("Длина пароля должна быть от 4 до 30 символов."));
    }

    @Test
    public void testGeneratePasswordInvalidComplexity() throws Exception {
        when(passwordService.generatePassword(8, 4)).thenThrow(new IllegalArgumentException("Уровень сложности должен быть от 1 до 3."));
        mockMvc.perform(get("/api/passwords/generate")
                        .param("length", "8")
                        .param("complexity", "4")
                        .param("owner", "user1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Уровень сложности должен быть от 1 до 3."));
    }

    @Test
    public void testGeneratePasswordsBulk() throws Exception {
        List<PasswordGenerationRequest> requests = Arrays.asList(
                new PasswordGenerationRequest(8, 2, "user1"),
                new PasswordGenerationRequest(10, 3, "user2")
        );
        List<String> passwords = Arrays.asList("password1", "password12");
        when(passwordService.generatePasswordsBulk(anyList())).thenReturn(passwords);

        String requestJson = objectMapper.writeValueAsString(requests);

        mockMvc.perform(post("/api/passwords/generate-bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0]").value("password1"))
                .andExpect(jsonPath("$[1]").value("password12"));
    }

    @Test
    public void testGeneratePasswordsBulkEmptyRequest() throws Exception {
        List<PasswordGenerationRequest> requests = List.of();
        when(passwordService.generatePasswordsBulk(anyList())).thenReturn(List.of());

        mockMvc.perform(post("/api/passwords/generate-bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void testGetAll() throws Exception {
        List<Password> passwords = Arrays.asList(
                new Password("pass1", "user1"),
                new Password("pass2", "user2")
        );
        when(passwordService.findAll()).thenReturn(passwords);

        mockMvc.perform(get("/api/passwords"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0].password").value("pass1"))
                .andExpect(jsonPath("$[0].owner").value("user1"))
                .andExpect(jsonPath("$[1].password").value("pass2"))
                .andExpect(jsonPath("$[1].owner").value("user2"));
    }

    @Test
    public void testGetAllEmpty() throws Exception {
        when(passwordService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/passwords"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void testGetByIdFound() throws Exception {
        Password password = new Password("pass1", "user1");
        password.setId(1L);
        when(passwordService.findById(1L)).thenReturn(Optional.of(password));

        mockMvc.perform(get("/api/passwords/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.password").value("pass1"))
                .andExpect(jsonPath("$.owner").value("user1"));
    }

    @Test
    public void testGetByIdNotFound() throws Exception {
        when(passwordService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/passwords/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreate() throws Exception {
        Password password = new Password("pass1", "user1");
        password.setId(1L);
        when(passwordService.create(any(Password.class))).thenReturn(password);

        mockMvc.perform(post("/api/passwords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(password)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.password").value("pass1"))
                .andExpect(jsonPath("$.owner").value("user1"));
    }

    @Test
    public void testCreateInvalidBody() throws Exception {
        String invalidJson = "{\"password\":null,\"owner\":null}";

        mockMvc.perform(post("/api/passwords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdate() throws Exception {
        Password password = new Password("updatedPass", "user1");
        password.setId(1L);
        when(passwordService.update(any(Password.class))).thenReturn(password);

        mockMvc.perform(put("/api/passwords/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(password)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.password").value("updatedPass"))
                .andExpect(jsonPath("$.owner").value("user1"));
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        Password password = new Password("pass1", "user1");
        when(passwordService.update(any(Password.class))).thenThrow(new IllegalArgumentException("Password not found"));

        mockMvc.perform(put("/api/passwords/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(password)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password not found"));
    }

    @Test
    public void testDelete() throws Exception {
        doNothing().when(passwordService).delete(1L);

        mockMvc.perform(delete("/api/passwords/1"))

                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetPasswordsByTagName() throws Exception {
        List<Password> passwords = Arrays.asList(
                new Password("pass1", "user1"),
                new Password("pass2", "user2")
        );
        when(passwordService.findPasswordsByTagName("tag1")).thenReturn(passwords);

        mockMvc.perform(get("/api/passwords/by-tag")
                        .param("tagName", "tag1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0].password").value("pass1"))
                .andExpect(jsonPath("$[0].owner").value("user1"))
                .andExpect(jsonPath("$[1].password").value("pass2"))
                .andExpect(jsonPath("$[1].owner").value("user2"));
    }

    @Test
    public void testGetPasswordsByTagNameEmpty() throws Exception {
        when(passwordService.findPasswordsByTagName("tag1")).thenReturn(List.of());

        mockMvc.perform(get("/api/passwords/by-tag")
                        .param("tagName", "tag1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$").isEmpty());
    }
}