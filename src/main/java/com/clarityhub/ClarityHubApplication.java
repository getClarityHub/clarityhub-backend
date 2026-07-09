package com.clarityhub;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(
        title = "ClarityHub API",
        version = "0.1.0",
        description = "Enterprise document intelligence with permission-scoped retrieval"
))
@SpringBootApplication
public class ClarityHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClarityHubApplication.class, args);
    }
}