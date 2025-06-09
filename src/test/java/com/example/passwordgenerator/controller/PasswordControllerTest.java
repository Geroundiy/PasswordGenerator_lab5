package com.example.passwordgenerator.controller;

import com.example.passwordgenerator.dto.PasswordGenerationRequest;
import com.example.passwordgenerator.entity.Password;
import com.example.passwordgenerator.service.PasswordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PasswordControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private PasswordController passwordController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(passwordController)
                .build();
    }

    @Test
    void testGeneratePassword() throws Exception {
        when(passwordService.generatePassword(8, 2, "user1")).thenReturn("password1");
        when(passwordService.create(any(Password.class)))
                .thenReturn(new Password("password1", "user1"));

        mockMvc.perform(get("/api/passwords/generate")
                        .param("length", "8")
                        .param("complexity", "2")
                        .param("owner", "user1"))
                .andExpect(status().isOk())
                .andExpect(content().string("✅ Пароль для user1: password1"));

        verify(passwordService).generatePassword(8, 2, "user1");
        verify(passwordService).create(any(Password.class));
    }

    @Test
    void testGeneratePasswordsBulk() throws Exception {
        var requests = List.of(
                new PasswordGenerationRequest(8, 2, "user1"),
                new PasswordGenerationRequest(10, 3, "user2")
        );
        var passwords = List.of("pwd1", "pwd2");
        when(passwordService.generatePasswordsBulk(requests)).thenReturn(passwords);

        mockMvc.perform(post("/api/passwords/generate-bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("pwd1"))
                .andExpect(jsonPath("$[1]").value("pwd2"));

        verify(passwordService).generatePasswordsBulk(requests);
    }

    @Test
    void testGetAllPasswords() throws Exception {
        var p1 = new Password("p1","u1"); p1.setId(1L);
        var p2 = new Password("p2","u2"); p2.setId(2L);
        when(passwordService.findAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/passwords"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].value").value("p1"))
                .andExpect(jsonPath("$[0].owner").value("u1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].value").value("p2"))
                .andExpect(jsonPath("$[1].owner").value("u2"));

        verify(passwordService).findAll();
    }

    @Test
    void testGetPasswordById() throws Exception {
        var p = new Password("secret","owner"); p.setId(5L);
        when(passwordService.findById(5L)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/api/passwords/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.value").value("secret"))
                .andExpect(jsonPath("$.owner").value("owner"));

        verify(passwordService).findById(5L);
    }

    @Test
    void testCreatePassword() throws Exception {
        Password toCreate = new Password("newpass","joe");
        when(passwordService.create(any(Password.class))).thenReturn(toCreate);

        mockMvc.perform(post("/api/passwords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("newpass"))
                .andExpect(jsonPath("$.owner").value("joe"));

        verify(passwordService).create(any(Password.class));
    }

    @Test
    void testUpdatePassword() throws Exception {
        Password updated = new Password("upd","mary");
        updated.setId(7L);
        when(passwordService.update(any(Password.class))).thenReturn(updated);

        mockMvc.perform(put("/api/passwords/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.value").value("upd"))
                .andExpect(jsonPath("$.owner").value("mary"));

        verify(passwordService).update(any(Password.class));
    }

    @Test
    void testDeletePassword() throws Exception {
        doNothing().when(passwordService).delete(3L);

        mockMvc.perform(delete("/api/passwords/3"))
                .andExpect(status().isNoContent());

        verify(passwordService).delete(3L);
    }
}
