package com.example.passwordgenerator.dto;

public class PasswordGenerationRequest {
    private int length;
    private int complexity;
    private String owner;

    // Конструктор по умолчанию
    public PasswordGenerationRequest() {}

    public PasswordGenerationRequest(int length, int complexity, String owner) {
        this.length = length;
        this.complexity = complexity;
        this.owner = owner;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getComplexity() {
        return complexity;
    }

    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}