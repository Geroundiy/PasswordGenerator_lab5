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
import static org.mockito.ArgumentMatchers.any;
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
        tagService = new TagService(tagRepository, tagCache);
    }

    @Test
    public void testFindAll() {
        List<Tag> tags = Arrays.asList(new Tag("tag1"), new Tag("tag2"));
        when(tagCache.getAllTags()).thenReturn(Optional.empty());
        when(tagRepository.findAll()).thenReturn(tags);
        List<Tag> result = tagService.findAll();
        if (result.size() != 2) {
            fail("Ожидается 2 тега, но получено " + result.size());
        }
        if (!"tag1".equals(result.get(0).getName())) {
            fail("Неверное имя тега: " + result.get(0).getName());
        }
        verify(tagCache).putAllTags(tags);
    }

    @Test
    public void testFindAllFromCache() {
        List<Tag> tags = Arrays.asList(new Tag("tag1"), new Tag("tag2"));
        when(tagCache.getAllTags()).thenReturn(Optional.of(tags));
        List<Tag> result = tagService.findAll();
        if (result.size() != 2) {
            fail("Ожидается 2 тега, но получено " + result.size());
        }
        if (!"tag1".equals(result.get(0).getName())) {
            fail("Неверное имя тега: " + result.get(0).getName());
        }
        verify(tagRepository, never()).findAll();
    }

    @Test
    public void testFindById() {
        Tag tag = new Tag("tag1");
        tag.setId(1L);
        when(tagCache.getTagById(1L)).thenReturn(Optional.empty());
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        Optional<Tag> result = tagService.findById(1L);
        if (!result.isPresent()) {
            fail("Тег должен быть найден!");
        }
        if (!"tag1".equals(result.get().getName())) {
            fail("Неверное имя тега: " + result.get().getName());
        }
        verify(tagCache).putTagById(1L, tag);
    }

    @Test
    public void testFindByIdFromCache() {
        Tag tag = new Tag("tag1");
        tag.setId(1L);
        when(tagCache.getTagById(1L)).thenReturn(Optional.of(tag));
        Optional<Tag> result = tagService.findById(1L);
        if (!result.isPresent()) {
            fail("Тег должен быть найден!");
        }
        if (!"tag1".equals(result.get().getName())) {
            fail("Неверное имя тега: " + result.get().getName());
        }
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
        if (!"newTag".equals(saved.getName())) {
            fail("Неверное имя тега: " + saved.getName());
        }
        if (saved.getId() != 1L) {
            fail("Неверный ID тега: " + saved.getId());
        }
        verify(tagCache).clearCache();
    }

    @Test
    public void testUpdate() {
        Tag tag = new Tag("updatedTag");
        tag.setId(1L);
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Tag updated = tagService.update(tag);
        if (!"updatedTag".equals(updated.getName())) {
            fail("Неверное имя тега: " + updated.getName());
        }
        if (updated.getId() != 1L) {
            fail("Неверный ID тега: " + updated.getId());
        }
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