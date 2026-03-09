package com.openclassrooms.datashare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.datashare.repository.FileLinkRepository;
import com.openclassrooms.datashare.service.FileLinkService;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class FileControllerTestIT {
    @Container
    static PostgreSQLContainer<?> psqlContainer = new PostgreSQLContainer<>("postgres:18.3");

    @Autowired
    private FileLinkService service;
    @Autowired
    private FileLinkRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> psqlContainer.getJdbcUrl());
        registry.add("spring.datasource.username", () -> psqlContainer.getUsername());
        registry.add("spring.datasource.password", () -> psqlContainer.getPassword());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @AfterEach
    public void afterEach() {
        repository.deleteAll();
    }
}
