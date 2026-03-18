package com.openclassrooms.datashare.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du validateur @MultipartFileIsCorrect.
 * La validation est portée par l'annotation : on teste via le mécanisme Jakarta Validation
 * sur un DTO portant l'annotation, ce qui correspond exactement au comportement en production.
 */
@ExtendWith(MockitoExtension.class)
public class MultipartFileValidatorTest {

    private Validator validator;

    /**
     * DTO minimal portant l'annotation — reproduit la structure de FileUploadDTO
     * sans ses autres contraintes, pour isoler uniquement @MultipartFileIsCorrect.
     */
    static class FileUploadRequest {
        @MultipartFileIsCorrect
        org.springframework.web.multipart.MultipartFile file;

        FileUploadRequest(org.springframework.web.multipart.MultipartFile file) {
            this.file = file;
        }
    }

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private MockMultipartFile buildFile(String filename, long sizeBytes) {
        // Génère un contenu de la taille voulue
        byte[] content = new byte[(int) sizeBytes];
        return new MockMultipartFile("file", filename, "application/octet-stream", content);
    }

    private Set<ConstraintViolation<FileUploadRequest>> validate(
            org.springframework.web.multipart.MultipartFile file) {
        return validator.validate(new FileUploadRequest(file));
    }

    // =========================================================================
    // Tests de validité
    // =========================================================================

    @Nested
    @Tag("MultipartFileValidator_ValidCases")
    @DisplayName("Cas valides")
    class ValidCases {

        @Test
        @DisplayName("Null est accepté (la contrainte @NotNull gère ce cas séparément)")
        void test_null_file_is_valid() {
            Set<ConstraintViolation<FileUploadRequest>> violations = validate(null);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Un fichier .txt de 1 Ko est valide")
        void test_txt_file_1kb_is_valid() {
            MockMultipartFile file = buildFile("document.txt", 1_000);
            assertThat(validate(file)).isEmpty();
        }

        @Test
        @DisplayName("Un fichier .pdf de 500 Ko est valide")
        void test_pdf_file_500kb_is_valid() {
            MockMultipartFile file = buildFile("rapport.pdf", 500_000);
            assertThat(validate(file)).isEmpty();
        }

        @Test
        @DisplayName("Un fichier .zip de 1 Go exactement est valide")
        void test_zip_file_1gb_exactly_is_valid() {
            MockMultipartFile file = buildFile("archive.zip", 1_000_000_000L);
            assertThat(validate(file)).isEmpty();
        }

        @Test
        @DisplayName("Toutes les extensions autorisées sont acceptées")
        void test_all_authorized_extensions_are_valid() {
            String[] extensions = {
                    "jpg", "jpeg", "png", "gif",
                    "doc", "docx", "dotx", "xltx", "ppt", "pptx", "potx", "ppsx",
                    "pdf", "html", "pages",
                    "xls", "xlsx", "xml", "zip", "csv", "txt",
                    "mp3", "mp4", "midi"
            };
            for (String ext : extensions) {
                MockMultipartFile file = buildFile("fichier." + ext, 5_000);
                assertThat(validate(file))
                        .as("Extension '%s' devrait être valide", ext)
                        .isEmpty();
            }
        }
    }

    // =========================================================================
    // Tests d'invalidity : nom de fichier
    // =========================================================================

    @Nested
    @Tag("MultipartFileValidator_FilenameCases")
    @DisplayName("Validation du nom de fichier")
    class FilenameCases {

        @Test
        @DisplayName("Un nom de fichier de 256 caractères (avec extension) est refusé")
        void test_filename_over_255_chars_is_invalid() {
            // 252 'a' + ".txt" = 256 caractères
            String longName = "a".repeat(252) + ".txt";
            MockMultipartFile file = buildFile(longName, 5_000);

            Set<ConstraintViolation<FileUploadRequest>> violations = validate(file);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .contains("255");
        }

        @Test
        @DisplayName("Un nom de fichier de 255 caractères exactement est accepté")
        void test_filename_exactly_255_chars_is_valid() {
            // 251 'a' + ".txt" = 255 caractères
            String name = "a".repeat(251) + ".txt";
            MockMultipartFile file = buildFile(name, 5_000);
            assertThat(validate(file)).isEmpty();
        }
    }

    // =========================================================================
    // Tests d'invalidity : extension
    // =========================================================================

    @Nested
    @Tag("MultipartFileValidator_ExtensionCases")
    @DisplayName("Validation de l'extension")
    class ExtensionCases {

        @Test
        @DisplayName("Une extension non autorisée (.exe) est refusée")
        void test_exe_extension_is_invalid() {
            MockMultipartFile file = buildFile("virus.exe", 5_000);

            Set<ConstraintViolation<FileUploadRequest>> violations = validate(file);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .contains("zip");
        }

        @Test
        @DisplayName("Une extension non autorisée (.sh) est refusée")
        void test_sh_extension_is_invalid() {
            MockMultipartFile file = buildFile("script.sh", 5_000);
            assertThat(validate(file)).isNotEmpty();
        }

        @Test
        @DisplayName("Un fichier sans extension est refusé")
        void test_file_without_extension_is_invalid() {
            // lastIndexOf(".") + 1 retourne l'intégralité du nom → extension inconnue
            MockMultipartFile file = buildFile("fichier_sans_extension", 5_000);
            assertThat(validate(file)).isNotEmpty();
        }
    }

    // =========================================================================
    // Tests d'invalidity : taille
    // =========================================================================

    @Nested
    @Tag("MultipartFileValidator_SizeCases")
    @DisplayName("Validation de la taille")
    class SizeCases {

        @Test
        @DisplayName("Un fichier de 999 octets (< 1 Ko) est refusé")
        void test_file_under_1kb_is_invalid() {
            MockMultipartFile file = buildFile("tiny.txt", 999);

            Set<ConstraintViolation<FileUploadRequest>> violations = validate(file);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .contains("1Ko");
        }

        @Test
        @DisplayName("Un fichier de 1 000 000 001 octets (> 1 Go) est refusé")
        void test_file_over_1gb_is_invalid() {
            // MockMultipartFile accepte un byte[] donc on ne peut pas dépasser Integer.MAX_VALUE,
            // mais on peut tester le validateur directement via un mock
            org.springframework.web.multipart.MultipartFile bigFile =
                    org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
            org.mockito.Mockito.when(bigFile.getOriginalFilename()).thenReturn("big.zip");
            org.mockito.Mockito.when(bigFile.getSize()).thenReturn(1_000_000_001L);

            Set<ConstraintViolation<FileUploadRequest>> violations = validate(bigFile);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .contains("1Go");
        }

        @Test
        @DisplayName("Un fichier de 0 octet est refusé (< 1 Ko)")
        void test_empty_file_is_invalid() {
            MockMultipartFile file = buildFile("empty.txt", 0);
            assertThat(validate(file)).isNotEmpty();
        }
    }

    // =========================================================================
    // Tests de cumul d'erreurs
    // =========================================================================

    @Nested
    @Tag("MultipartFileValidator_CumulativeErrors")
    @DisplayName("Cumul de violations")
    class CumulativeErrors {

        @Test
        @DisplayName("Un fichier trop petit avec une extension non autorisée génère 2 violations")
        void test_small_file_with_bad_extension_generates_two_violations() {
            MockMultipartFile file = buildFile("tiny.exe", 500);

            Set<ConstraintViolation<FileUploadRequest>> violations = validate(file);
            assertThat(violations).hasSize(2);
        }
    }
}