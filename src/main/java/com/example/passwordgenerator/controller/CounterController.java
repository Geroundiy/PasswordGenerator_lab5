package com.example.passwordgenerator.controller;

import com.example.passwordgenerator.service.CounterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/counter")
public class CounterController {

    private final CounterService counterService;

    public CounterController(CounterService counterService) {
        this.counterService = counterService;
    }

    @GetMapping
    public ResponseEntity<Long> getCount() {
        long count = counterService.getRequestCount();
        return ResponseEntity.ok(count);
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> resetCount() {
        counterService.resetRequestCount();
        return ResponseEntity.noContent().build();
    }
}