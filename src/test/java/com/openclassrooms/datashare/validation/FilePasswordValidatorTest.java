package com.openclassrooms.datashare.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du validateur @FilePassword.
 * Le mot de passe est optionnel : vide/null est toujours valide.
 * S'il est fourni, il doit faire au minimum 6 caractères.
 */
@ExtendWith(MockitoExtension.class)
public class FilePasswordValidatorTest {

    private Validator validator;

    static class PasswordRequest {
        @FilePassword
        String password;

        PasswordRequest(String password) {
            this.password = password;
        }
    }

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Set<ConstraintViolation<PasswordRequest>> validate(String password) {
        return validator.validate(new PasswordRequest(password));
    }

    // =========================================================================
    // Cas valides
    // =========================================================================

    @Nested
    @Tag("FilePasswordValidator_ValidCases")
    @DisplayName("Cas valides")
    class ValidCases {

        @Test
        @DisplayName("Null est valide (le mot de passe est optionnel)")
        void test_null_password_is_valid() {
            assertThat(validate(null)).isEmpty();
        }

        @Test
        @DisplayName("Une chaîne vide est valide (le mot de passe est optionnel)")
        void test_empty_password_is_valid() {
            assertThat(validate("")).isEmpty();
        }

        @Test
        @DisplayName("Une chaîne de blancs est valide (le mot de passe est optionnel)")
        void test_blank_password_is_valid() {
            assertThat(validate("   ")).isEmpty();
        }

        @Test
        @DisplayName("Un mot de passe de 6 caractères exactement est valide")
        void test_password_6_chars_is_valid() {
            assertThat(validate("abc123")).isEmpty();
        }

        @Test
        @DisplayName("Un mot de passe de plus de 6 caractères est valide")
        void test_password_more_than_6_chars_is_valid() {
            assertThat(validate("motDePasseSécurisé!")).isEmpty();
        }
    }

    // =========================================================================
    // Cas invalides
    // =========================================================================

    @Nested
    @Tag("FilePasswordValidator_InvalidCases")
    @DisplayName("Cas invalides")
    class InvalidCases {

        @Test
        @DisplayName("Un mot de passe de 5 caractères est refusé")
        void test_password_5_chars_is_invalid() {
            Set<ConstraintViolation<PasswordRequest>> violations = validate("abc12");
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .contains("6");
        }

        @Test
        @DisplayName("Un mot de passe de 1 caractère est refusé")
        void test_password_1_char_is_invalid() {
            assertThat(validate("x")).isNotEmpty();
        }
    }
}