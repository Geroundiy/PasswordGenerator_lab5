package com.example.passwordgenerator.service;

import com.example.passwordgenerator.cache.TagCache;
import com.example.passwordgenerator.entity.Tag;
import com.example.passwordgenerator.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final TagCache tagCache;

    public TagService(TagRepository tagRepository, TagCache tagCache) {
        this.tagRepository = tagRepository;
        this.tagCache = tagCache;
    }

    public List<Tag> findAll() {
        Optional<List<Tag>> cachedTags = tagCache.getAllTags();
        if (cachedTags.isPresent()) {
            return cachedTags.get();
        }
        List<Tag> tags = tagRepository.findAll();
        tagCache.putAllTags(tags);
        return tags;
    }

    public Optional<Tag> findById(Long id) {
        Optional<Tag> cachedTag = tagCache.getTagById(id);
        if (cachedTag.isPresent()) {
            return cachedTag;
        }
        Optional<Tag> tag = tagRepository.findById(id);
        tag.ifPresent(t -> tagCache.putTagById(id, t));
        return tag;
    }

    public Tag create(Tag tag) {
        Tag saved = tagRepository.save(tag);
        tagCache.clearCache();
        return saved;
    }

    public Tag update(Tag tag) {
        Tag saved = tagRepository.save(tag);
        tagCache.clearCache();
        return saved;
    }

    public void delete(Long id) {
        tagRepository.deleteById(id);
        tagCache.clearCache();
    }
}