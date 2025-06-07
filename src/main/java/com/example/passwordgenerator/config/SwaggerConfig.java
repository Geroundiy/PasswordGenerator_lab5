package com.example.passwordgenerator.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "Password Generator API",
                version = "1.0",
                description = "API для генерации паролей"
        )
)
public class SwaggerConfig {
}