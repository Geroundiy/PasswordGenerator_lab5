package com.example.passwordgenerator.controller;

import com.example.passwordgenerator.entity.Tag;
import com.example.passwordgenerator.service.TagService;
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
class TagControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TagService tagService;

    @InjectMocks
    private TagController tagController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(tagController)
                .build();
    }

    @Test
    void testGetAllTags() throws Exception {
        Tag t1 = new Tag("tag1"); t1.setId(1L);
        Tag t2 = new Tag("tag2"); t2.setId(2L);
        when(tagService.findAll()).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("tag1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("tag2"));

        verify(tagService).findAll();
    }

    @Test
    void testGetTagById() throws Exception {
        Tag t = new Tag("alpha"); t.setId(10L);
        when(tagService.findById(10L)).thenReturn(Optional.of(t));

        mockMvc.perform(get("/api/tags/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("alpha"));

        verify(tagService).findById(10L);
    }

    @Test
    void testCreateTag() throws Exception {
        Tag incoming = new Tag("newTag");
        Tag created = new Tag("newTag"); created.setId(5L);
        when(tagService.create(any(Tag.class))).thenReturn(created);

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incoming)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("newTag"));

        verify(tagService).create(any(Tag.class));
    }

    @Test
    void testUpdateTag() throws Exception {
        Tag upd = new Tag("updTag");
        upd.setId(8L);
        when(tagService.update(any(Tag.class))).thenReturn(upd);

        mockMvc.perform(put("/api/tags/8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.name").value("updTag"));

        verify(tagService).update(any(Tag.class));
    }

    @Test
    void testDeleteTag() throws Exception {
        doNothing().when(tagService).delete(4L);

        mockMvc.perform(delete("/api/tags/4"))
                .andExpect(status().isNoContent());

        verify(tagService).delete(4L);
    }
}
