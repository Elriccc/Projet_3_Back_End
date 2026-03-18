package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.entities.FileLink;
import com.openclassrooms.datashare.entities.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @TempDir
    Path tempDir;

    private FileService service;

    @BeforeEach
    void setUp() {
        service = new FileService();
        ReflectionTestUtils.setField(service, "DATA_PATH", tempDir.toString());
        ReflectionTestUtils.setField(service, "NO_USER_DIRECTORY", "/anonymous/");
    }

    @Nested
    @DisplayName("Ajouter un fichier")
    class AddFileTests {

        @Test
        @DisplayName("Avec un utilisateur ajoute le fichier dans son répertoire")
        void addFile_withUser_createsFileInUserDirectory() throws IOException {
            User user = buildUser("user-abc");
            FileLink fl = buildFileLink(user, "file-001", "txt");

            MockMultipartFile multipart = new MockMultipartFile(
                    "file", "test.txt", "text/plain", "Hello World".getBytes());

            service.addFile(fl, multipart);

            Path expected = tempDir.resolve("user-abc").resolve("file-001.txt");
            assertThat(expected).exists();
            assertThat(Files.readString(expected)).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("Sans utilisateur ajoute le fichier dans le répertoire anonyme")
        void addFile_anonymous_createsFileInAnonymousDirectory() {
            FileLink fl = buildFileLink(null, "file-anon", "png");

            MockMultipartFile multipart = new MockMultipartFile(
                    "file", "image.png", "image/png", "PNG_BYTES".getBytes());

            service.addFile(fl, multipart);

            Path expected = tempDir.resolve("anonymous").resolve("file-anon.png");
            assertThat(expected).exists();
        }

        @Test
        @DisplayName("Crée le répertoire parent s'il n'existe pas")
        void addFile_createsParentDirectoryIfMissing() {
            User user = buildUser("new-user");
            FileLink fl = buildFileLink(user, "brand-new", "csv");

            MockMultipartFile multipart = new MockMultipartFile(
                    "file", "data.csv", "text/csv", "col1,col2".getBytes());

            assertThatCode(() -> service.addFile(fl, multipart)).doesNotThrowAnyException();
            assertThat(tempDir.resolve("new-user").resolve("brand-new.csv")).exists();
        }
    }

    @Nested
    @DisplayName("Récupèrer le flux du fichier")
    class GetFileStreamTests {

        @Test
        @DisplayName("Avec un fichier existant renvoie le flux")
        void getFileStream_existingFile_returnsStream() throws IOException {
            User user = buildUser("user-abc");
            FileLink fl = buildFileLink(user, "stream-file", "txt");

            Path dir = tempDir.resolve("user-abc");
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("stream-file.txt"), "stream content");

            InputStreamResource resource = service.getFileStream(fl);

            assertThat(resource).isNotNull();
            assertThat(resource.getInputStream()).isNotNull();
        }

        @Test
        @DisplayName("Avec un fichier introuvable lève une erreur aucun lien trouvé")
        void getFileStream_missingFile_throwsNoSuchElement() {
            User user = buildUser("user-abc");
            FileLink fl = buildFileLink(user, "ghost", "txt");
            // No physical file written

            assertThatThrownBy(() -> service.getFileStream(fl))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("ghost.txt");
        }
    }

    @Nested
    @DisplayName("Supprimer un fichier")
    class DeleteFileTests {

        @Test
        @DisplayName("En fournissant un fichier existant le supprime")
        void deleteFile_existingFile_deletesFile() throws IOException {
            Path dir = tempDir.resolve("user-del");
            Files.createDirectories(dir);
            Path file = dir.resolve("to-delete.txt");
            Files.writeString(file, "bye");

            service.deleteFile("user-del/to-delete.txt");

            assertThat(file).doesNotExist();
        }

        @Test
        @DisplayName("En fournissant un fichier inexistant ne lève pas d'exception")
        void deleteFile_nonExistingFile_doesNotThrow() {
            assertThatCode(() -> service.deleteFile("ghost/none.txt"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Supprimer un fichier pour le CRON")
    class DeleteFileFromJobTests {

        @Test
        @DisplayName("Supprime un fichier physique lié")
        void deleteFileFromJob_existingFile_deletesFile() throws IOException {
            User user = buildUser("job-user");
            FileLink fl = buildFileLink(user, "job-file", "pdf");

            Path dir = tempDir.resolve("job-user");
            Files.createDirectories(dir);
            Path file = dir.resolve("job-file.pdf");
            Files.writeString(file, "pdf bytes");

            service.deleteFileFromJob(List.of(fl));

            assertThat(file).doesNotExist();
        }
    }

    private User buildUser(String id) {
        User u = new User();
        u.setId(id);
        u.setLogin(id + "@test.com");
        u.setPassword("encoded");
        return u;
    }

    private FileLink buildFileLink(User user, String id, String extension) {
        FileLink fl = new FileLink();
        fl.setId(id);
        fl.setName(id);
        fl.setExtension(extension);
        fl.setSize(100L);
        fl.setFileLink("lnk12");
        fl.setUsePassword(false);
        fl.setIsExpired(false);
        fl.setUser(user);
        return fl;
    }
}