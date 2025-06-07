package com.example.passwordgenerator.service;

import com.example.passwordgenerator.counter.RequestCounterInterface;
import org.springframework.stereotype.Service;

@Service
public class CounterService {

    private final RequestCounterInterface requestCounter;

    public CounterService(RequestCounterInterface requestCounter) {
        this.requestCounter = requestCounter;
    }

    public long getRequestCount() {
        return requestCounter.getCount();
    }

    public void resetRequestCount() {
        requestCounter.reset();
    }
}
