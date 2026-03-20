package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.configuration.security.AuthenticationService;
import com.openclassrooms.datashare.entities.FileLink;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.handler.ExpiredLinkException;
import com.openclassrooms.datashare.repository.FileLinkRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileLinkServiceTest {

    private static final String FILE_LINK_PATH  = "abc12";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedPasswordHash";
    private static final String PLAIN_PASSWORD   = "mySecret";

    @Mock private FileLinkRepository  repository;
    @Mock private PasswordEncoder     pwdEncoder;
    @Mock private AuthenticationService authenticationService;

    @InjectMocks private FileLinkService service;

    @BeforeEach
    void startUp(){
        ReflectionTestUtils.setField(service, "FILE_LINK_LENGTH", "5");
    }

    @Nested
    @DisplayName("Téléverser un fichier")
    class SaveFileLinkTests {

        @Test
        @DisplayName("Sans mot de passe, enregistre le fichier sans encodage")
        void saveFileLink_withoutPassword_savesWithoutEncoding() {
            FileLink fl = buildFileLink(null, false);
            User user = buildUser("u1");

            when(authenticationService.getUserIfExist(any())).thenReturn(user);
            when(repository.save(any(FileLink.class))).thenAnswer(inv -> inv.getArgument(0));

            FileLink result = service.saveFileLink("Bearer token", fl);

            verify(pwdEncoder, never()).encode(any());
            assertThat(result.getUsePassword()).isFalse();
            assertThat(result.getUser()).isEqualTo(user);
            assertThat(result.getIsExpired()).isFalse();
            assertThat(result.getFileLink()).isNotNull().hasSize(5);
        }

        @Test
        @DisplayName("Avec un mot de passe, encode le mot de passe avant sauvegarde")
        void saveFileLink_withPassword_encodesPassword() {
            FileLink fl = buildFileLink(PLAIN_PASSWORD, true);
            User user = buildUser("u1");

            when(authenticationService.getUserIfExist(any())).thenReturn(user);
            when(pwdEncoder.encode(PLAIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(repository.save(any(FileLink.class))).thenAnswer(inv -> inv.getArgument(0));

            FileLink result = service.saveFileLink("Bearer token", fl);

            verify(pwdEncoder).encode(PLAIN_PASSWORD);
            assertThat(result.getPassword()).isEqualTo(ENCODED_PASSWORD);
            assertThat(result.getUsePassword()).isTrue();
        }

        @Test
        @DisplayName("Sans utilisateur (anonyme), enregistre avec user=null")
        void saveFileLink_anonymous_savesWithNullUser() {
            FileLink fl = buildFileLink(null, false);

            when(authenticationService.getUserIfExist(any())).thenReturn(null);
            when(repository.save(any(FileLink.class))).thenAnswer(inv -> inv.getArgument(0));

            FileLink result = service.saveFileLink(null, fl);

            assertThat(result.getUser()).isNull();
        }
    }

    @Nested
    @DisplayName("Récupérer tous les fichiers d'un utilisateur")
    class GetAllFileLinksByAccountTests {

        @Test
        @DisplayName("Avec un utilisateur authentifié renvoie ses fichiers")
        void getAllFileLinksByAccount_authenticated_returnsFiles() {
            User user = buildUser("u1");
            List<FileLink> files = List.of(buildFileLink(null, false), buildFileLink(null, false));

            when(authenticationService.getUserIfExist(any())).thenReturn(user);
            when(repository.getFileLinksByUser(user)).thenReturn(files);

            List<FileLink> result = service.getAllFileLinksByAccount("Bearer token");

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Avec un utilisateur anonyme renvoie une liste vide")
        void getAllFileLinksByAccount_anonymous_returnsEmptyList() {
            when(authenticationService.getUserIfExist(any())).thenReturn(null);

            List<FileLink> result = service.getAllFileLinksByAccount(null);

            assertThat(result).isEmpty();
            verify(repository, never()).getFileLinksByUser(any());
        }
    }

    @Nested
    @DisplayName("Récupérer un fichier")
    class GetFileLinkTests {

        @Test
        @DisplayName("Avec un lien valide non expiré renvoie le FileLink")
        void getFileLink_valid_returnsFileLink() {
            FileLink fl = buildFileLinkWithExpiration(LocalDate.now().plusDays(2));
            when(repository.findByFileLink(FILE_LINK_PATH)).thenReturn(Optional.of(fl));

            FileLink result = service.getFileLink(FILE_LINK_PATH);

            assertThat(result).isEqualTo(fl);
        }

        @Test
        @DisplayName("Avec un lien introuvable lève une exception aucun lien trouvé")
        void getFileLink_notFound_throwsNoSuchElement() {
            when(repository.findByFileLink(FILE_LINK_PATH)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getFileLink(FILE_LINK_PATH))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("Avec un lien expiré lève une exception lien expiré")
        void getFileLink_expired_throwsExpiredLinkException() {
            FileLink fl = buildFileLinkWithExpiration(LocalDate.now().minusDays(1));
            when(repository.findByFileLink(FILE_LINK_PATH)).thenReturn(Optional.of(fl));

            assertThatThrownBy(() -> service.getFileLink(FILE_LINK_PATH))
                    .isInstanceOf(ExpiredLinkException.class);
        }
    }

    @Nested
    @DisplayName("Supprimer un fichier")
    class DeleteFileLinkTests {

        @Test
        @DisplayName("Avec un propriétaire autorisé supprime et renvoie le chemin du fichier")
        void deleteFileLink_owner_deletesAndReturnsPath() {
            User user = buildUser("user-id-1");
            FileLink fl = buildFileLinkWithUser(user);
            fl.setId("file-id-1");
            fl.setExtension("txt");

            when(authenticationService.getUserIfExist(any())).thenReturn(user);
            when(repository.findByFileLink(FILE_LINK_PATH)).thenReturn(Optional.of(fl));

            String path = service.deleteFileLink("Bearer token", FILE_LINK_PATH);

            verify(repository).delete(fl);
            assertThat(path).isEqualTo("user-id-1/file-id-1.txt");
        }

        @Test
        @DisplayName("Avec un utilisateur non propriétaire lève une exception mauvaise identification")
        void deleteFileLink_notOwner_throwsBadCredentials() {
            User owner    = buildUser("owner-id");
            User intruder = buildUser("intruder-id");
            FileLink fl   = buildFileLinkWithUser(owner);

            when(authenticationService.getUserIfExist(any())).thenReturn(intruder);
            when(repository.findByFileLink(FILE_LINK_PATH)).thenReturn(Optional.of(fl));

            assertThatThrownBy(() -> service.deleteFileLink("Bearer bad", FILE_LINK_PATH))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("Avec un utilisateur anonyme lève une exception mauvaise identification")
        void deleteFileLink_anonymous_throwsBadCredentials() {
            User owner  = buildUser("owner-id");
            FileLink fl = buildFileLinkWithUser(owner);

            when(authenticationService.getUserIfExist(any())).thenReturn(null);
            when(repository.findByFileLink(FILE_LINK_PATH)).thenReturn(Optional.of(fl));

            assertThatThrownBy(() -> service.deleteFileLink(null, FILE_LINK_PATH))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("Avec un lien introuvable lève une exception aucun lien trouvé")
        void deleteFileLink_notFound_throwsNoSuchElement() {
            User user = buildUser("u1");

            when(authenticationService.getUserIfExist(any())).thenReturn(user);
            when(repository.findByFileLink(FILE_LINK_PATH)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteFileLink("Bearer token", FILE_LINK_PATH))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("Mis à jour des tags")
    class UpdateFileLinkTagsTests {

        @Test
        @DisplayName("Avec un propriétaire autorisé met à jour les tags")
        void updateFileLinkTags_owner_updatesTags() {
            User user   = buildUser("u1");
            FileLink fl = buildFileLinkWithUser(user);
            List<String> newTags = List.of("a", "b", "c");

            when(authenticationService.getUserIfExist(any())).thenReturn(user);
            when(repository.findByFileLink(FILE_LINK_PATH)).thenReturn(Optional.of(fl));
            when(repository.save(any(FileLink.class))).thenAnswer(inv -> inv.getArgument(0));

            FileLink result = service.updateFileLinkTags("Bearer token", FILE_LINK_PATH, newTags);

            assertThat(result.getTags()).containsExactly("a", "b", "c");
            verify(repository).save(fl);
        }

        @Test
        @DisplayName("Avec un utilisateur non propriétaire lève une exception mauvaise identification")
        void updateFileLinkTags_notOwner_throwsBadCredentials() {
            User owner    = buildUser("owner-id");
            User intruder = buildUser("intruder-id");
            FileLink fl   = buildFileLinkWithUser(owner);

            when(authenticationService.getUserIfExist(any())).thenReturn(intruder);
            when(repository.findByFileLink(FILE_LINK_PATH)).thenReturn(Optional.of(fl));

            assertThatThrownBy(() -> service.updateFileLinkTags("Bearer bad", FILE_LINK_PATH, List.of("x")))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("Vérifier que le mot de passe est incorrect")
    class IsPasswordIncorrectTests {

        @Test
        @DisplayName("Avec un fichier sans mot de passe renvoie faux")
        void isPasswordIncorrect_noPasswordRequired_returnsFalse() {
            FileLink fl = buildFileLink(null, false);

            assertThat(service.isPasswordIncorrect(fl, null)).isFalse();
            assertThat(service.isPasswordIncorrect(fl, "anything")).isFalse();
        }

        @Test
        @DisplayName("Avec un fichier avec mot de passe null renvoie vrai")
        void isPasswordIncorrect_passwordRequired_nullInput_returnsTrue() {
            FileLink fl = buildFileLink(ENCODED_PASSWORD, true);

            assertThat(service.isPasswordIncorrect(fl, null)).isTrue();
        }

        @Test
        @DisplayName("Avec un fichier avec mot de passe vide renvoie vrai")
        void isPasswordIncorrect_passwordRequired_emptyInput_returnsTrue() {
            FileLink fl = buildFileLink(ENCODED_PASSWORD, true);

            assertThat(service.isPasswordIncorrect(fl, "")).isTrue();
            assertThat(service.isPasswordIncorrect(fl, "   ")).isTrue();
        }

        @Test
        @DisplayName("Avec un fichier avec un mauvais mot de passe renvoie vrai")
        void isPasswordIncorrect_passwordRequired_wrongInput_returnsTrue() {
            FileLink fl = buildFileLink(ENCODED_PASSWORD, true);
            when(pwdEncoder.matches("wrong", ENCODED_PASSWORD)).thenReturn(false);

            assertThat(service.isPasswordIncorrect(fl, "wrong")).isTrue();
        }

        @Test
        @DisplayName("Avec un fichier avec un bon mot de passe renvoie faux")
        void isPasswordIncorrect_passwordRequired_correctInput_returnsFalse() {
            FileLink fl = buildFileLink(ENCODED_PASSWORD, true);
            when(pwdEncoder.matches(PLAIN_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

            assertThat(service.isPasswordIncorrect(fl, PLAIN_PASSWORD)).isFalse();
        }
    }

    private FileLink buildFileLink(String password, boolean usePassword) {
        FileLink fl = new FileLink();
        fl.setId("test");
        fl.setName("file");
        fl.setExtension("txt");
        fl.setSize(100L);
        fl.setPassword(password);
        fl.setUsePassword(usePassword);
        fl.setExpirationDate(LocalDate.now().plusDays(3));
        fl.setIsExpired(false);
        fl.setTags(List.of());
        return fl;
    }

    private FileLink buildFileLinkWithExpiration(LocalDate date) {
        FileLink fl = buildFileLink(null, false);
        fl.setExpirationDate(date);
        return fl;
    }

    private FileLink buildFileLinkWithUser(User user) {
        FileLink fl = buildFileLink(null, false);
        fl.setUser(user);
        return fl;
    }

    private User buildUser(String id) {
        User u = new User();
        u.setId(id);
        u.setLogin(id + "@test.com");
        u.setPassword("encoded");
        return u;
    }
}