package com.openclassrooms.datashare.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MultipartFileServiceTest {

    @InjectMocks
    MultipartFileService service;

    @Nested
    @Tag("UploadTests")
    @DisplayName("Téléverser un fichier")
    class UploadTests {

        @Test
        @DisplayName("Sans inclure de fichier renvoie une erreur")
        public void test_upload_null_file_throws_IllegalArgumentException() {

        }
    }

    @Nested
    @Tag("DownloadTests")
    @DisplayName("Télécharger un fichier")
    class DownloadTests {

    }
}
