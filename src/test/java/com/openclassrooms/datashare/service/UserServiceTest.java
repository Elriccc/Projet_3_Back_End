package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.configuration.security.CustomJwtService;
import com.openclassrooms.datashare.configuration.security.CustomUserDetailService;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    private static final String LOGIN = "test@gmail.com";
    private static final String PASSWORD = "correctPassword1234*";
    private static final String JWT = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJUZXN0VXNlciIsImlhdCI6MTc3MDkxMjQ0OCwiZXhwIjoxNzcwOTE2MDQ4fQ.y9y3IlLH5exRutwafO1tL33mEyFYcFdx0NQotj06y1I";

    @Mock
    private UserRepository repository;
    @Mock
    private AuthenticationProvider authenticationManager;
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
        @DisplayName("Avec un nouveau login fonctionne")
        public void test_create_user() {
            // GIVEN
            User user = new User();
            user.setLogin(LOGIN);
            user.setPassword(PASSWORD);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD);

            // WHEN
            service.register(user);

            // THEN
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(repository).save(userCaptor.capture());
            assertThat(userCaptor.getValue()).isEqualTo(user);
        }
    }

    @Nested
    @Tag("LoginTests")
    @DisplayName("Se connecter")
    class LoginTests {

        @Test
        @DisplayName("Avec un login et un mot de passe existant fonctionne")
        public void test_connect_user(){
            // GIVEN
            User user = new User();
            user.setLogin(LOGIN);
            user.setPassword(PASSWORD);
            when(repository.findByLogin(any())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(true);
            lenient().when(jwtUtils.generateToken(any(UserDetails.class))).thenReturn(JWT);

            //WHEN
            final String result = service.login(LOGIN, PASSWORD);

            //THEN
            verify(authenticationManager, times(1)).authenticate(any(Authentication.class));
            verify(jwtUtils, times(1)).generateToken(any(UserDetails.class));
            assertThat(result).isEqualTo(JWT);
        }
    }
}
