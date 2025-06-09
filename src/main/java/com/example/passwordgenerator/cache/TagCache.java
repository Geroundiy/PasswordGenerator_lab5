package com.example.passwordgenerator.cache;

import com.example.passwordgenerator.entity.Tag;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class TagCache {
    private final Map<String, Object> cache = new HashMap<>();

    public Optional<List<Tag>> getAllTags() {
        return Optional.ofNullable((List<Tag>) cache.get("allTags"));
    }

    public void putAllTags(List<Tag> tags) {
        cache.put("allTags", tags);
    }

    public Optional<Tag> getTagById(Long id) {
        return Optional.ofNullable((Tag) cache.get("tag_" + id));
    }

    public void putTagById(Long id, Tag tag) {
        cache.put("tag_" + id, tag);
    }

    public void clearCache() {
        cache.clear();
    }
}