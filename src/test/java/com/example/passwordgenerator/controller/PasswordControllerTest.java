package com.example.passwordgenerator.controller;

import com.example.passwordgenerator.service.PasswordService;
import com.example.passwordgenerator.dto.PasswordGenerationRequest;
import com.example.passwordgenerator.entity.Password;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ObjectMapper objectMapper;

    private PasswordController passwordController;

    @BeforeEach
    public void setUp() {
        passwordController = new PasswordController(passwordService);
        mockMvc = MockMvcBuilders.standaloneSetup(passwordController).build();
    }

    @Test
    public void testGeneratePassword() throws Exception {
        when(passwordService.generatePassword(8, 2)).thenReturn("password");
        when(passwordService.create(any(Password.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(get("/api/passwords/generate")
                        .param("length", "8")
                        .param("complexity", "2")
                        .param("owner", "user1"))
                .andExpect(status().isOk())
                .andExpect(content().string("✅ Пароль для user1: password"));
    }

    @Test
    public void testGeneratePasswordInvalidLength() throws Exception {
        mockMvc.perform(get("/api/passwords/generate")
                        .param("length", "3")
                        .param("complexity", "2")
                        .param("owner", "user1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Длина пароля должна быть от 4 до 30 символов."));
    }

    @Test
    public void testGeneratePasswordsBulk() throws Exception {
        List<PasswordGenerationRequest> requests = Arrays.asList(
                new PasswordGenerationRequest(8, 2, "user1"),
                new PasswordGenerationRequest(10, 3, "user2")
        );
        List<String> passwords = Arrays.asList("password1", "password12");
        when(passwordService.generatePasswordsBulk(requests)).thenReturn(passwords);

        mockMvc.perform(post("/api/passwords/generate-bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("password1"))
                .andExpect(jsonPath("$[1]").value("password12"));
    }

    @Test
    public void testGetAllPasswords() throws Exception {
        List<Password> passwords = Arrays.asList(
                new Password("pass1", "user1"),
                new Password("pass2", "user2")
        );
        when(passwordService.findAll()).thenReturn(passwords);

        mockMvc.perform(get("/api/passwords"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].password").value("pass1"))
                .andExpect(jsonPath("$[1].password").value("pass2"));
    }

    @Test
    public void testGetPasswordById() throws Exception {
        Password password = new Password("pass1", "user1");
        password.setId(1L);
        when(passwordService.findById(1L)).thenReturn(Optional.of(password));

        mockMvc.perform(get("/api/passwords/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").value("pass1"));
    }

    @Test
    public void testGetPasswordByIdNotFound() throws Exception {
        when(passwordService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/passwords/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreatePassword() throws Exception {
        Password password = new Password("pass1", "user1");
        when(passwordService.create(any(Password.class))).thenReturn(password);

        mockMvc.perform(post("/api/passwords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").value("pass1"));
    }

    @Test
    public void testUpdatePassword() throws Exception {
        Password password = new Password("pass1", "user1");
        password.setId(1L);
        when(passwordService.update(any(Password.class))).thenReturn(password);

        mockMvc.perform(put("/api/passwords/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").value("pass1"));
    }

    @Test
    public void testDeletePassword() throws Exception {
        doNothing().when(passwordService).delete(1L);

        mockMvc.perform(delete("/api/passwords/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetPasswordsByTagName() throws Exception {
        List<Password> passwords = Arrays.asList(new Password("pass1", "user1"));
        when(passwordService.findPasswordsByTagName("tag1")).thenReturn(passwords);

        mockMvc.perform(get("/api/passwords/by-tag")
                        .param("tagName", "tag1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].password").value("pass1"));
    }
}