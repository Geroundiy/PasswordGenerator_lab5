package com.example.passwordgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class PasswordGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PasswordGeneratorApplication.class, args);
    }
}