package com.example.passwordgenerator.controller;

import com.example.passwordgenerator.service.TagService;
import com.example.passwordgenerator.entity.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
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

public class TagControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TagService tagService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private TagController tagController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        tagController = new TagController(tagService);
        mockMvc = MockMvcBuilders.standaloneSetup(tagController).build();
    }

    @Test
    public void testGetAllTags() throws Exception {
        List<Tag> tags = Arrays.asList(new Tag("tag1"), new Tag("tag2"));
        when(tagService.findAll()).thenReturn(tags);
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("tag1"))
                .andExpect(jsonPath("$[1].name").value("tag2"));
    }

    @Test
    public void testGetTagById() throws Exception {
        Tag tag = new Tag("tag1");
        tag.setId(1L);
        when(tagService.findById(1L)).thenReturn(Optional.of(tag));
        mockMvc.perform(get("/api/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("tag1"));
    }

    @Test
    public void testGetTagByIdNotFound() throws Exception {
        when(tagService.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/tags/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateTag() throws Exception {
        Tag tag = new Tag("newTag");
        when(tagService.create(any(Tag.class))).thenReturn(tag);
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("newTag"));
    }

    @Test
    public void testUpdateTag() throws Exception {
        Tag tag = new Tag("updatedTag");
        tag.setId(1L);
        when(tagService.update(any(Tag.class))).thenReturn(tag);

        mockMvc.perform(put("/api/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updatedTag"));
    }

    @Test
    public void testDeleteTag() throws Exception {
        doNothing().when(tagService).delete(1L);

        mockMvc.perform(delete("/api/tags/1"))
                .andExpect(status().isNoContent());
    }
}