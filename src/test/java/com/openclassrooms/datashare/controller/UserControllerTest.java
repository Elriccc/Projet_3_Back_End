package com.openclassrooms.datashare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.datashare.dto.UserDTO;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.repository.UserRepository;
import com.openclassrooms.datashare.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class UserControllerTest {
    @Container
    static PostgreSQLContainer psqlContainer = new PostgreSQLContainer("postgres:18.3");

    private static final String URL_REGISTER = "/api/register";
    private static final String URL_LOGIN = "/api/login";
    private static final String LOGIN = "test@gmail.com";
    private static final String PASSWORD = "correctPassword1234*";

    @Autowired
    private UserService service;
    @Autowired
    private UserRepository repository;
    private ObjectMapper objectMapper = new ObjectMapper();
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

    @Nested
    @DisplayName("Créer un utilisateur")
    class RegisterTest {
        @DisplayName("Avec des données manquantes renvoie une erreur")
        @Test
        public void registerUserWithoutRequiredData() throws Exception {
            // GIVEN
            UserDTO userDTO = new UserDTO();

            // WHEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL_REGISTER)
                            .content(objectMapper.writeValueAsString(userDTO))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @DisplayName("Existant déjà renvoie une erreur")
        @Test
        public void registerAlreadyExistUser() throws Exception {
            // GIVEN
            User user = new User();
            user.setLogin(LOGIN);
            user.setPassword(PASSWORD);
            service.register(user);

            UserDTO userDTO = new UserDTO();
            userDTO.setLogin(LOGIN);
            userDTO.setPassword(PASSWORD);

            // WHEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL_REGISTER)
                            .content(objectMapper.writeValueAsString(userDTO))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @DisplayName("Avec des données correctes fonctionne")
        @Test
        public void registerUserSuccessful() throws Exception {
            // GIVEN
            UserDTO userDTO = new UserDTO();
            userDTO.setLogin(LOGIN);
            userDTO.setPassword(PASSWORD);

            // WHEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL_REGISTER)
                            .content(objectMapper.writeValueAsString(userDTO))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isCreated());
        }
    }
}
