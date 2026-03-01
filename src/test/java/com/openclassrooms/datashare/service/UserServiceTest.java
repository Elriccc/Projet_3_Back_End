package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.configuration.security.CustomJwtService;
import com.openclassrooms.datashare.configuration.security.CustomUserDetailService;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.repository.UserRepository;
import com.openclassrooms.datashare.validator.UserValidator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    private static final String LOGIN = "test@gmail.com";
    private static final String PASSWORD = "correctPassword1234*";

    @Mock
    private UserValidator validator;
    @Mock
    private UserRepository repository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private CustomUserDetailService userDetailService;
    @Mock
    private CustomJwtService jwtUtils;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService service;

    @Nested
    @Tag("RegisterTests")
    @DisplayName("Créer un utilisateur")
    class RegisterTests {
        @Test
        @DisplayName("Sans paramètres renvoie une erreur")
        public void test_create_null_user_throws_IllegalArgumentException() {
            // GIVEN

            // THEN
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> service.register(null));
        }

        @Test
        @DisplayName("Avec un login existant renvoie une erreur")
        public void test_create_already_exist_user_throws_IllegalArgumentException() {
            // GIVEN
            User user = new User();
            user.setLogin(LOGIN);
            user.setPassword(PASSWORD);
            when(repository.findByLogin(any())).thenReturn(Optional.of(user));

            // THEN
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> service.register(user));
        }

        @Test
        @DisplayName("Avec un nouveau login fonctionne")
        public void test_create_user() {
            // GIVEN
            User user = new User();
            user.setLogin(LOGIN);
            user.setPassword(PASSWORD);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD);
            when(repository.findByLogin(any())).thenReturn(Optional.empty());

            // WHEN
            service.register(user);

            // THEN
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(repository).save(userCaptor.capture());
            assertThat(userCaptor.getValue()).isEqualTo(user);
        }
    }
}
