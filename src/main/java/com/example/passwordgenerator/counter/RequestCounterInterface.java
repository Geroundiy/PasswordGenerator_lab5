package com.example.passwordgenerator.counter;

public interface RequestCounterInterface {
    long increment();
    long getCount();
    void reset();
}
