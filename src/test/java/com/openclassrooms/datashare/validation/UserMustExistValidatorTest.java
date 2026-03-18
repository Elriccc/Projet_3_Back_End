package com.openclassrooms.datashare.validation;

import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.repository.UserRepository;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserMustExistValidatorTest {

    private static final String LOGIN           = "user@gmail.com";
    private static final String PLAIN_PASSWORD  = "plainPassword";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedHash";

    @Mock private UserRepository              repository;
    @Mock private PasswordEncoder             passwordEncoder;
    @Mock private ConstraintValidatorContext  context;
    @Mock private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;
    @Mock private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;
    @Mock private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder2;

    @InjectMocks private UserMustExistValidator validator;

    /** Simulates the annotation attributes as set on UserDTO. */
    @BeforeEach
    void init() {
        UserMustExist annotation = mock(UserMustExist.class);
        when(annotation.login()).thenReturn("login");
        when(annotation.password()).thenReturn("password");
        validator.initialize(annotation);
    }

        // Holder class mirroring the annotated UserDTO structure
        record Credentials(String login, String password) {
    }

    // =========================================================================
    // Cas valides (délégation à d'autres contraintes ou combinaison correcte)
    // =========================================================================

    @Nested
    @Tag("UserMustExistValidator_ValidCases")
    @DisplayName("Cas valides")
    class ValidCases {

        @Test
        @DisplayName("Login null est valide (délégué à @NotBlank)")
        void test_null_login_is_valid() {
            assertThat(validator.isValid(new Credentials(null, PLAIN_PASSWORD), context)).isTrue();
            verify(repository, never()).findByLogin(any());
        }

        @Test
        @DisplayName("Password null est valide (délégué à @NotBlank)")
        void test_null_password_is_valid() {
            assertThat(validator.isValid(new Credentials(LOGIN, null), context)).isTrue();
            verify(repository, never()).findByLogin(any());
        }

        @Test
        @DisplayName("Login vide est valide (délégué à @NotBlank)")
        void test_empty_login_is_valid() {
            assertThat(validator.isValid(new Credentials("", PLAIN_PASSWORD), context)).isTrue();
            verify(repository, never()).findByLogin(any());
        }

        @Test
        @DisplayName("Password vide est valide (délégué à @NotBlank)")
        void test_empty_password_is_valid() {
            assertThat(validator.isValid(new Credentials(LOGIN, ""), context)).isTrue();
            verify(repository, never()).findByLogin(any());
        }

        @Test
        @DisplayName("Login/mot de passe corrects renvoie true")
        void test_correct_credentials_is_valid() {
            User user = buildUser();
            when(repository.findByLogin(LOGIN)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PLAIN_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

            assertThat(validator.isValid(new Credentials(LOGIN, PLAIN_PASSWORD), context)).isTrue();
        }
    }

    // =========================================================================
    // Cas invalides
    // =========================================================================

    @Nested
    @Tag("UserMustExistValidator_InvalidCases")
    @DisplayName("Cas invalides")
    class InvalidCases {

        /** Stub the constraint violation builder chain used inside the validator. */
        private void stubViolationBuilder() {
            when(context.getDefaultConstraintMessageTemplate()).thenReturn("Login ou mot de passe incorrect");
            when(context.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);
            when(violationBuilder.addPropertyNode(any())).thenReturn(nodeBuilder);
            when(nodeBuilder.addPropertyNode(any())).thenReturn(nodeBuilder2);
        }

        @Test
        @DisplayName("Utilisateur inexistant en base renvoie false")
        void test_unknown_user_is_invalid() {
            stubViolationBuilder();
            when(repository.findByLogin(LOGIN)).thenReturn(Optional.empty());

            assertThat(validator.isValid(new Credentials(LOGIN, PLAIN_PASSWORD), context)).isFalse();
            verify(context).disableDefaultConstraintViolation();
        }

        @Test
        @DisplayName("Mot de passe incorrect renvoie false")
        void test_wrong_password_is_invalid() {
            stubViolationBuilder();
            User user = buildUser();
            when(repository.findByLogin(LOGIN)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PLAIN_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

            assertThat(validator.isValid(new Credentials(LOGIN, PLAIN_PASSWORD), context)).isFalse();
            verify(context).disableDefaultConstraintViolation();
        }

        @Test
        @DisplayName("Les noeuds de violation sont correctement construits lors d'un échec")
        void test_violation_nodes_are_built_on_failure() {
            stubViolationBuilder();
            when(repository.findByLogin(LOGIN)).thenReturn(Optional.empty());

            validator.isValid(new Credentials(LOGIN, PLAIN_PASSWORD), context);

            verify(violationBuilder).addPropertyNode("login");
            verify(nodeBuilder).addPropertyNode("password");
            verify(nodeBuilder2).addConstraintViolation();
        }
    }

    private User buildUser() {
        User u = new User();
        u.setId("some-uuid");
        u.setLogin(UserMustExistValidatorTest.LOGIN);
        u.setPassword(UserMustExistValidatorTest.ENCODED_PASSWORD);
        return u;
    }
}