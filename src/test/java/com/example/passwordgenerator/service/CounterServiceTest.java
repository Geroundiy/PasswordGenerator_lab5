package com.example.passwordgenerator.service;

import com.example.passwordgenerator.counter.RequestCounterInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CounterServiceTest {

    @Mock
    private RequestCounterInterface requestCounter;

    @InjectMocks
    private CounterService counterService;

    @Test
    public void testGetRequestCount() {
        when(requestCounter.getCount()).thenReturn(5L);
        assertEquals(5L, counterService.getRequestCount());
    }

    @Test
    public void testResetRequestCount() {
        counterService.resetRequestCount();
        verify(requestCounter).reset();
    }
}
