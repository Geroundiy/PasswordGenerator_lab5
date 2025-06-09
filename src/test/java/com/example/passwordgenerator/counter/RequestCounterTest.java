package com.example.passwordgenerator.counter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestCounterTest {

    @Mock
    private RequestCounter counter;

    @BeforeEach
    public void setUp() {
        when(counter.increment()).thenReturn(1L, 2L);
        when(counter.getCount()).thenReturn(0L, 1L, 2L, 100L);
        doNothing().when(counter).reset();
    }

    @Test
    public void testIncrement() {
        counter.reset();
        long count = counter.increment();
        assertEquals(1, count, "Счетчик должен увеличиться до 1");
        count = counter.increment();
        assertEquals(2, count, "Счетчик должен увеличиться до 2");
    }

    @Test
    public void testGetCount() {
        counter.reset();
        counter.increment();
        counter.increment();
        assertEquals(2, counter.getCount(), "Счетчик должен быть равен 2");
    }

    @Test
    public void testReset() {
        counter.increment();
        counter.increment();
        counter.reset();
        assertEquals(0, counter.getCount(), "Счетчик должен быть сброшен до 0");
    }

    @Test
    public void testConcurrentIncrement() throws InterruptedException {
        when(counter.getCount()).thenReturn(100L);
        int threadCount = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                counter.increment();
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        assertEquals(threadCount, counter.getCount(), "Счетчик должен быть равен количеству потоков");
    }
}