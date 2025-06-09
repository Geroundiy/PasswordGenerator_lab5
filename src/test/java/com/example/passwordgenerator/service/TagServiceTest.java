package com.example.passwordgenerator.service;

import com.example.passwordgenerator.cache.TagCache;
import com.example.passwordgenerator.entity.Tag;
import com.example.passwordgenerator.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagCache tagCache;

    @InjectMocks
    private TagService tagService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindAll() {
        List<Tag> tags = Arrays.asList(new Tag("tag1"), new Tag("tag2"));
        when(tagCache.getAllTags()).thenReturn(Optional.empty());
        when(tagRepository.findAll()).thenReturn(tags);

        List<Tag> result = tagService.findAll();
        assertEquals(2, result.size());
        assertEquals("tag1", result.get(0).getName());
        verify(tagCache).putAllTags(tags);
    }

    @Test
    public void testUpdateTagNotFound() {
        Tag tag = new Tag("newTag");
        tag.setId(999L);
        when(tagRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> tagService.update(tag));
    }

    @Test
    public void testFindAllFromCache() {
        List<Tag> tags = Arrays.asList(new Tag("tag1"), new Tag("tag2"));
        when(tagCache.getAllTags()).thenReturn(Optional.of(tags));

        List<Tag> result = tagService.findAll();
        assertEquals(2, result.size());
        assertEquals("tag1", result.get(0).getName());
        verify(tagRepository, never()).findAll();
    }

    @Test
    public void testFindById() {
        Tag tag = new Tag("tag1");
        tag.setId(1L);
        when(tagCache.getTagById(1L)).thenReturn(Optional.empty());
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        Optional<Tag> result = tagService.findById(1L);
        assertTrue(result.isPresent());
        assertEquals("tag1", result.get().getName());
        verify(tagCache).putTagById(1L, tag);
    }

    @Test
    public void testFindByIdFromCache() {
        Tag tag = new Tag("tag1");
        tag.setId(1L);
        when(tagCache.getTagById(1L)).thenReturn(Optional.of(tag));

        Optional<Tag> result = tagService.findById(1L);
        assertTrue(result.isPresent());
        assertEquals("tag1", result.get().getName());
        verify(tagRepository, never()).findById(anyLong());
    }

    @Test
    public void testCreate() {
        Tag tag = new Tag("newTag");
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        Tag saved = tagService.create(tag);
        assertEquals("newTag", saved.getName());
        assertEquals(1L, saved.getId());
        verify(tagCache).clearCache();
    }

    @Test
    public void testUpdate() {
        Tag tag = new Tag("updatedTag");
        tag.setId(1L);
        when(tagRepository.existsById(1L)).thenReturn(true);
        when(tagRepository.save(any(Tag.class))).thenReturn(tag);

        Tag updated = tagService.update(tag);
        assertEquals("updatedTag", updated.getName());
        assertEquals(1L, updated.getId());
        verify(tagCache).clearCache();
    }

    @Test
    public void testDelete() {
        doNothing().when(tagRepository).deleteById(1L);
        tagService.delete(1L);
        verify(tagRepository).deleteById(1L);
        verify(tagCache).clearCache();
    }
}