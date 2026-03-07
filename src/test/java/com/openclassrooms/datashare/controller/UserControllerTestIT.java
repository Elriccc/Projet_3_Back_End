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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("test")
@Testcontainers
public class UserControllerTestIT {

    @Container
    static PostgreSQLContainer<?> psqlContainer = new PostgreSQLContainer<>("postgres:18.3");

    private static final String URL_REGISTER = "/api/register";
    private static final String URL_LOGIN = "/api/login";
    private static final String LOGIN = "test@gmail.com";
    private static final String PASSWORD = "correctPassword1234*";

    @Autowired
    private UserService service;
    @Autowired
    private UserRepository repository;
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
                    .andExpect(MockMvcResultMatchers.status().is5xxServerError());
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

    @Nested
    @DisplayName("Se connecter")
    class LoginTest {
        @DisplayName("Sans mettre de login renvoie une erreur")
        @Test
        public void loginUserNoLogin() throws Exception {
            // GIVEN
            UserDTO userDTO = new UserDTO();
            userDTO.setPassword(PASSWORD);

            // WHEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL_LOGIN)
                            .content(objectMapper.writeValueAsString(userDTO))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @DisplayName("Sans mettre de mot de passe renvoie une erreur")
        @Test
        public void loginUserNoPassword() throws Exception {
            // GIVEN
            UserDTO userDTO = new UserDTO();
            userDTO.setLogin(LOGIN);

            // WHEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL_LOGIN)
                            .content(objectMapper.writeValueAsString(userDTO))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @DisplayName("À un utilisateur qui n'existe pas renvoie une erreur")
        @Test
        public void loginUserUnknownUser() throws Exception {
            // GIVEN
            UserDTO userDTO = new UserDTO();
            userDTO.setLogin(LOGIN);
            userDTO.setPassword(PASSWORD);

            // WHEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL_LOGIN)
                            .content(objectMapper.writeValueAsString(userDTO))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @DisplayName("Avec un mauvais mot de passe renvoie une erreur")
        @Test
        public void loginUserBadPassword() throws Exception {
            // GIVEN
            final String BAD_PASSWORD = "Badpassword";
            UserDTO registerDTO = new UserDTO();
            registerDTO.setLogin(LOGIN);
            registerDTO.setPassword(PASSWORD);
            mockMvc.perform(MockMvcRequestBuilders.post(URL_REGISTER)
                    .content(objectMapper.writeValueAsString(registerDTO))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON));
            UserDTO userDTO = new UserDTO();
            userDTO.setLogin(LOGIN);
            userDTO.setPassword(BAD_PASSWORD);

            // WHEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL_LOGIN)
                            .content(objectMapper.writeValueAsString(userDTO))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @DisplayName("Avec des informations correctes fonctionne")
        @Test
        public void loginUserSuccessful() throws Exception {
            // GIVEN
            UserDTO registeredUserDTO = new UserDTO();
            registeredUserDTO.setLogin(LOGIN);
            registeredUserDTO.setPassword(PASSWORD);
            mockMvc.perform(MockMvcRequestBuilders.post(URL_REGISTER)
                    .content(objectMapper.writeValueAsString(registeredUserDTO))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON));
            UserDTO userDTO = new UserDTO();
            userDTO.setLogin(LOGIN);
            userDTO.setPassword(PASSWORD);

            // WHEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL_LOGIN)
                            .content(objectMapper.writeValueAsString(userDTO))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
        }
    }
}
