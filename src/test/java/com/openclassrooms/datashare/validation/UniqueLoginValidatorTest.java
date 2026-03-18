package com.openclassrooms.datashare.validation;

import com.openclassrooms.datashare.repository.UserRepository;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UniqueLoginValidatorTest {

    private static final String EXISTING_LOGIN    = "existing@gmail.com";
    private static final String NON_EXISTING_LOGIN = "new@gmail.com";

    @Mock private UserRepository            repository;
    @Mock private ConstraintValidatorContext context;

    @InjectMocks private UniqueLoginValidator validator;

    @Nested
    @Tag("UniqueLoginValidator_ValidCases")
    @DisplayName("Cas valides")
    class ValidCases {

        @Test
        @DisplayName("Login null est valide (délégué à @NotBlank)")
        void test_null_login_is_valid() {
            assertThat(validator.isValid(null, context)).isTrue();
            verify(repository, never()).existsByLogin(any());
        }

        @Test
        @DisplayName("Login vide est valide (délégué à @NotBlank)")
        void test_empty_login_is_valid() {
            assertThat(validator.isValid("", context)).isTrue();
            verify(repository, never()).existsByLogin(any());
        }

        @Test
        @DisplayName("Login en blanc est valide (délégué à @NotBlank)")
        void test_blank_login_is_valid() {
            assertThat(validator.isValid("   ", context)).isTrue();
            verify(repository, never()).existsByLogin(any());
        }

        @Test
        @DisplayName("Login inexistant en base est valide")
        void test_nonExisting_login_is_valid() {
            when(repository.existsByLogin(NON_EXISTING_LOGIN)).thenReturn(false);

            assertThat(validator.isValid(NON_EXISTING_LOGIN, context)).isTrue();
        }
    }

    // =========================================================================
    // Cas invalides
    // =========================================================================

    @Nested
    @Tag("UniqueLoginValidator_InvalidCases")
    @DisplayName("Cas invalides")
    class InvalidCases {

        @Test
        @DisplayName("Login déjà présent en base est invalide")
        void test_existing_login_is_invalid() {
            when(repository.existsByLogin(EXISTING_LOGIN)).thenReturn(true);

            assertThat(validator.isValid(EXISTING_LOGIN, context)).isFalse();
        }
    }
}