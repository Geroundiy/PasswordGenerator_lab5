package com.example.passwordgenerator.counter;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RequestCounter implements RequestCounterInterface {
    private final AtomicLong counter = new AtomicLong(0);

    @Override
    public long increment() {
        return counter.incrementAndGet();
    }

    @Override
    public long getCount() {
        return counter.get();
    }

    @Override
    public void reset() {
        counter.set(0);
    }
}
