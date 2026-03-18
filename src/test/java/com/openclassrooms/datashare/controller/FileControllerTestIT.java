package com.openclassrooms.datashare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.datashare.dto.UserDTO;
import com.openclassrooms.datashare.entities.FileLink;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.repository.FileLinkRepository;
import com.openclassrooms.datashare.repository.UserRepository;
import com.openclassrooms.datashare.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class FileControllerTestIT {

    @Container
    static PostgreSQLContainer<?> psqlContainer = new PostgreSQLContainer<>("postgres:18.3");

    private static final String URL_FILES         = "/api/files";
    private static final String URL_DOWNLOAD      = "/api/files/download/{fileLinkPath}";
    private static final String URL_LOGIN         = "/api/login";
    private static final String LOGIN             = "filetest@gmail.com";
    private static final String PASSWORD          = "correctPassword1234*";
    private static final String OTHER_LOGIN       = "other@gmail.com";

    @Autowired private MockMvc           mockMvc;
    @Autowired private UserService       userService;
    @Autowired private UserRepository    userRepository;
    @Autowired private FileLinkRepository fileLinkRepository;

    @Value("${app.data-path}")
    private String dataPath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      psqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", psqlContainer::getUsername);
        registry.add("spring.datasource.password", psqlContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    /** Registers a user and returns his JWT token. */
    private void register(String login){
        User user = new User();
        user.setLogin(login);
        user.setPassword(FileControllerTestIT.PASSWORD);
        userService.register(user);
    }

    private String login(String login) throws Exception {
        UserDTO dto = new UserDTO();
        dto.setLogin(login);
        dto.setPassword(FileControllerTestIT.PASSWORD);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(URL_LOGIN)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    /** Creates a FileLink in DB and the matching physical file in the data directory. */
    private FileLink createFileLink(User user, String filename) throws IOException {
        FileLink fl = new FileLink();
        fl.setName(filename);
        fl.setExtension("txt");
        fl.setSize(100L);
        fl.setFileLink("test1");
        fl.setUsePassword(false);
        fl.setPassword(null);
        fl.setExpirationDate(LocalDate.now().plusDays(3));
        fl.setIsExpired(false);
        fl.setTags(List.of("tag1"));
        fl.setUser(user);

        FileLink saved = fileLinkRepository.save(fl);

        // Write a physical file so download tests work
        Path dir = Path.of(dataPath, user != null ? user.getId() : "no-user");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(saved.getId() + ".txt"), "hello file content");

        return saved;
    }

    @AfterEach
    void afterEach() {
        fileLinkRepository.deleteAll();
        userRepository.deleteAll();
        // Clean up physical files
        File dataDir = new File(dataPath);
        if (dataDir.exists()) {
            for (File sub : Objects.requireNonNull(dataDir.listFiles())) {
                for (File f : Objects.requireNonNull(sub.listFiles())) f.delete();
                sub.delete();
            }
        }
    }

    @Nested
    @DisplayName("Ajouter un fichier")
    class AddFileTest {

        @Test
        @DisplayName("Sans fichier renvoie 400")
        void addFile_noFile_returns400() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.multipart(URL_FILES)
                            .param("expirationTime", "1"))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Avec un expirationTime invalide (0) renvoie 400")
        void addFile_invalidExpiration_returns400() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes())
            {@Override public long getSize() { return 1024L * 1024L; }}; // 1 Mo

            mockMvc.perform(MockMvcRequestBuilders.multipart(URL_FILES)
                            .file(file)
                            .param("expirationTime", "0")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Avec un expirationTime dépassant 7 renvoie 400")
        void addFile_expirationAbove7_returns400() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes())
            {@Override public long getSize() { return 1024L * 1024L; }}; // 1 Mo

            mockMvc.perform(MockMvcRequestBuilders.multipart(URL_FILES)
                            .file(file)
                            .param("expirationTime", "8")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Avec un fichier de moins d'1Ko renvoie une erreur")
        void addFile_fileBelow1Ko_returns400() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes())
            {@Override public long getSize() { return 10L; }}; // 10 octets

            mockMvc.perform(MockMvcRequestBuilders.multipart(URL_FILES)
                            .file(file)
                            .param("expirationTime", "7")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Avec un fichier de plus d'1Go renvoie 400")
        void addFile_fileAbove1Go_returns400() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes())
            {@Override public long getSize() { return 1024L * 1024L * 1024L * 1024L; }}; // 1 To

            mockMvc.perform(MockMvcRequestBuilders.multipart(URL_FILES)
                            .file(file)
                            .param("expirationTime", "7")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Sans authentification fonctionne (fichier anonyme)")
        void addFile_anonymous_returns201() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "image.png", MediaType.IMAGE_PNG_VALUE, "img-bytes".getBytes())
            {@Override public long getSize() { return 1024L * 1024L; }}; // 1 Mo

            mockMvc.perform(MockMvcRequestBuilders.multipart(URL_FILES)
                            .file(file)
                            .param("expirationTime", "2")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(jsonPath("$.name").value("image"))
                    .andExpect(jsonPath("$.extension").value("png"));
        }

        @Test
        @DisplayName("Avec authentification fonctionne et associe l'utilisateur")
        void addFile_authenticated_returns201() throws Exception {
            register(LOGIN);
            String token = login(LOGIN);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "doc.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf-bytes".getBytes())
            {@Override public long getSize() { return 1024L * 1024L; }}; // 1 Mo

            mockMvc.perform(MockMvcRequestBuilders.multipart(URL_FILES)
                            .file(file)
                            .param("expirationTime", "3")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(jsonPath("$.name").value("doc"))
                    .andExpect(jsonPath("$.extension").value("pdf"));
        }

        @Test
        @DisplayName("Avec un mot de passe trop court renvoie 400")
        void addFile_shortPassword_returns400() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes())
            {@Override public long getSize() { return 1024L * 1024L; }}; // 1 Mo

            mockMvc.perform(MockMvcRequestBuilders.multipart(URL_FILES)
                            .file(file)
                            .param("expirationTime", "1")
                            .param("password", "abc")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Avec un mot de passe valide fonctionne et indique usePassword=true")
        void addFile_withPassword_returns201() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "secret.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes())
            {@Override public long getSize() { return 1024L * 1024L; }}; // 1 Mo

            mockMvc.perform(MockMvcRequestBuilders.multipart(URL_FILES)
                            .file(file)
                            .param("expirationTime", "1")
                            .param("password", "securePass")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(jsonPath("$.usePassword").value(true));
        }
    }

    // =========================================================================
    // GET /api/files  –  retrieveAllFiles
    // =========================================================================

    @Nested
    @DisplayName("Récupérer tous les fichiers")
    class RetrieveAllFilesTest {

        @Test
        @DisplayName("Sans authentification renvoie une liste vide")
        void retrieveAll_anonymous_returnsEmptyList() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(URL_FILES))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("Avec authentification renvoie les fichiers de l'utilisateur")
        void retrieveAll_authenticated_returnsUserFiles() throws Exception {
            register(LOGIN);
            String token = login(LOGIN);
            User user = userRepository.findByLogin(LOGIN).stream().findFirst().orElseThrow();
            createFileLink(user, "myfile");

            mockMvc.perform(MockMvcRequestBuilders.get(URL_FILES)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("myfile"));
        }

        @Test
        @DisplayName("Avec authentification ne renvoie pas les fichiers des autres utilisateurs")
        void retrieveAll_authenticated_doesNotReturnOtherUsersFiles() throws Exception {
            register(LOGIN);
            register(OTHER_LOGIN);
            User other = userRepository.findByLogin(OTHER_LOGIN).stream().findFirst().orElseThrow();
            createFileLink(other, "otherfile");

            // Login as first user: should NOT see other's file
            String token = login(LOGIN);
            // Re-login since afterEach would clear — actually both are still registered here
            mockMvc.perform(MockMvcRequestBuilders.get(URL_FILES)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // =========================================================================
    // GET /api/files/{fileLinkPath}  –  retrieveFileByLink
    // =========================================================================

    @Nested
    @DisplayName("Récupérer un fichier par son lien")
    class RetrieveFileByLinkTest {

        @Test
        @DisplayName("Avec un lien inexistant renvoie 404")
        void getByLink_unknownLink_returns404() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(URL_FILES + "/XXXXX"))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        @DisplayName("Avec un lien valide renvoie le fichier")
        void getByLink_validLink_returns200() throws Exception {
            register(LOGIN);
            User user = userRepository.findByLogin(LOGIN).stream().findFirst().orElseThrow();
            FileLink fl = createFileLink(user, "myfound");

            mockMvc.perform(MockMvcRequestBuilders.get(URL_FILES + "/" + fl.getFileLink()))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$.name").value("myfound"));
        }

        @Test
        @DisplayName("Avec un lien expiré renvoie 410")
        void getByLink_expiredLink_returns410() throws Exception {
            register(LOGIN);
            User user = userRepository.findByLogin(LOGIN).stream().findFirst().orElseThrow();

            FileLink fl = new FileLink();
            fl.setName("expired");
            fl.setExtension("txt");
            fl.setSize(10L);
            fl.setFileLink("expir");
            fl.setUsePassword(false);
            fl.setExpirationDate(LocalDate.now().minusDays(1));
            fl.setIsExpired(true);
            fl.setTags(List.of());
            fl.setUser(user);
            fileLinkRepository.save(fl);

            mockMvc.perform(MockMvcRequestBuilders.get(URL_FILES + "/expir"))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isGone());
        }
    }

    @Nested
    @DisplayName("Télécharger un fichier")
    class DownloadFileTest {

        @Test
        @DisplayName("Avec un lien inexistant renvoie 404")
        void download_unknownLink_returns404() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.post(URL_DOWNLOAD, "XXXXX")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        @DisplayName("Fichier sans mot de passe téléchargeable sans body")
        void download_noPassword_returns200() throws Exception {
            register(LOGIN);
            User user = userRepository.findByLogin(LOGIN).stream().findFirst().orElseThrow();
            FileLink fl = createFileLink(user, "todownload");

            mockMvc.perform(MockMvcRequestBuilders.post(URL_DOWNLOAD, fl.getFileLink())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.CONTENT_DISPOSITION));
        }

        @Test
        @DisplayName("Fichier avec mot de passe et bon mot de passe renvoie 200")
        void download_correctPassword_returns200() throws Exception {
            register(LOGIN);
            String token = login(LOGIN);
            userRepository.findByLogin(LOGIN).stream().findFirst().orElseThrow();

            // Upload a file with password through the API so the password gets encoded
            MockMultipartFile file = new MockMultipartFile(
                    "file", "protected.txt", MediaType.TEXT_PLAIN_VALUE, "secret content".getBytes())
            {@Override public long getSize() { return 1024L * 1024L; }}; // 1 Mo
            MvcResult uploadResult = mockMvc.perform(MockMvcRequestBuilders.multipart(URL_FILES)
                            .file(file)
                            .param("expirationTime", "1")
                            .param("password", "validPwd")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andReturn();

            String responseBody = uploadResult.getResponse().getContentAsString();
            String fileLink = objectMapper.readTree(responseBody).get("fileLink").asText();

            mockMvc.perform(MockMvcRequestBuilders.post(URL_DOWNLOAD, fileLink)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("validPwd"))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        @Test
        @DisplayName("Fichier avec mot de passe et mauvais mot de passe renvoie 400")
        void download_wrongPassword_returns400() throws Exception {
            register(LOGIN);
            String token = login(LOGIN);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "protected2.txt", MediaType.TEXT_PLAIN_VALUE, "secret".getBytes())
            {@Override public long getSize() { return 1024L * 1024L; }}; // 1 Mo
            MvcResult uploadResult = mockMvc.perform(MockMvcRequestBuilders.multipart(URL_FILES)
                            .file(file)
                            .param("expirationTime", "1")
                            .param("password", "validPwd")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andReturn();

            String fileLink = objectMapper.readTree(
                    uploadResult.getResponse().getContentAsString()).get("fileLink").asText();

            mockMvc.perform(MockMvcRequestBuilders.post(URL_DOWNLOAD, fileLink)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("\"wrongPwd\""))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Fichier avec mot de passe sans fournir de mot de passe renvoie 400")
        void download_missingPassword_returns400() throws Exception {
            register(LOGIN);
            String token = login(LOGIN);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "protected3.txt", MediaType.TEXT_PLAIN_VALUE, "secret".getBytes())
            {@Override public long getSize() { return 1024L * 1024L; }}; // 1 Mo
            MvcResult uploadResult = mockMvc.perform(MockMvcRequestBuilders.multipart(URL_FILES)
                            .file(file)
                            .param("expirationTime", "1")
                            .param("password", "validPwd")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andReturn();

            String fileLink = objectMapper.readTree(
                    uploadResult.getResponse().getContentAsString()).get("fileLink").asText();

            mockMvc.perform(MockMvcRequestBuilders.post(URL_DOWNLOAD, fileLink)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }
    }

    // =========================================================================
    // DELETE /api/files/{fileLinkPath}  –  deleteFile
    // =========================================================================

    @Nested
    @DisplayName("Supprimer un fichier")
    class DeleteFileTest {

        @Test
        @DisplayName("Sans authentification renvoie 401")
        void delete_unauthenticated_returns401() throws Exception {
            register(LOGIN);
            User user = userRepository.findByLogin(LOGIN).stream().findFirst().orElseThrow();
            FileLink fl = createFileLink(user, "todelete");

            mockMvc.perform(MockMvcRequestBuilders.delete(URL_FILES + "/" + fl.getFileLink()))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        }

        @Test
        @DisplayName("Avec un autre utilisateur renvoie 401")
        void delete_otherUser_returns401() throws Exception {
            register(LOGIN);
            User user = userRepository.findByLogin(LOGIN).stream().findFirst().orElseThrow();
            FileLink fl = createFileLink(user, "todelete");

            register((OTHER_LOGIN));
            String otherToken = login(OTHER_LOGIN);

            mockMvc.perform(MockMvcRequestBuilders.delete(URL_FILES + "/" + fl.getFileLink())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        }

        @Test
        @DisplayName("Avec le bon utilisateur fonctionne et renvoie 200")
        void delete_owner_returns200() throws Exception {
            register(LOGIN);
            String token = login(LOGIN);
            User user = userRepository.findByLogin(LOGIN).stream().findFirst().orElseThrow();
            FileLink fl = createFileLink(user, "todelete");

            mockMvc.perform(MockMvcRequestBuilders.delete(URL_FILES + "/" + fl.getFileLink())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        @Test
        @DisplayName("Avec un lien inexistant renvoie 404")
        void delete_unknownLink_returns404() throws Exception {
            register(LOGIN);
            String token = login(LOGIN);

            mockMvc.perform(MockMvcRequestBuilders.delete(URL_FILES + "/XXXXX")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Mettre à jour les tags")
    class UpdateTagsTest {

        @Test
        @DisplayName("Sans authentification renvoie 401")
        void updateTags_unauthenticated_returns401() throws Exception {
            register(LOGIN);
            User user = userRepository.findByLogin(LOGIN).stream().findFirst().orElseThrow();
            FileLink fl = createFileLink(user, "tagged");

            mockMvc.perform(MockMvcRequestBuilders.put(URL_FILES + "/" + fl.getFileLink())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[\"newtag\"]"))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        }

        @Test
        @DisplayName("Avec le bon utilisateur fonctionne et renvoie les tags mis à jour")
        void updateTags_owner_returns200WithUpdatedTags() throws Exception {
            register(LOGIN);
            String token = login(LOGIN);
            User user = userRepository.findByLogin(LOGIN).stream().findFirst().orElseThrow();
            FileLink fl = createFileLink(user, "tagged");

            mockMvc.perform(MockMvcRequestBuilders.put(URL_FILES + "/" + fl.getFileLink())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[\"alpha\", \"beta\"]"))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(jsonPath("$.tags[0]").value("alpha"))
                    .andExpect(jsonPath("$.tags[1]").value("beta"));
        }

        @Test
        @DisplayName("Avec un autre utilisateur renvoie 401")
        void updateTags_otherUser_returns401() throws Exception {
            register(LOGIN);
            User user = userRepository.findByLogin(LOGIN).stream().findFirst().orElseThrow();
            FileLink fl = createFileLink(user, "tagged");

            register(OTHER_LOGIN);
            String otherToken = login(OTHER_LOGIN);

            mockMvc.perform(MockMvcRequestBuilders.put(URL_FILES + "/" + fl.getFileLink())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[\"hack\"]"))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        }

        @Test
        @DisplayName("Avec un lien inexistant renvoie 404")
        void updateTags_unknownLink_returns404() throws Exception {
            register(LOGIN);
            String token = login(LOGIN);

            mockMvc.perform(MockMvcRequestBuilders.put(URL_FILES + "/XXXXX")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[\"tag\"]"))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }
    }
}