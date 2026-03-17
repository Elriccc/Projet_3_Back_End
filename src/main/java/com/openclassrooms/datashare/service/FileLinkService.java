package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.configuration.security.AuthenticationService;
import com.openclassrooms.datashare.entities.FileLink;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.handler.ExpiredLinkException;
import com.openclassrooms.datashare.repository.FileLinkRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FileLinkService {
    private final FileLinkRepository repository;
    private final PasswordEncoder pwdEncoder;
    private final AuthenticationService authenticationService;

    public FileLink saveFileLink(String authHeader, FileLink fileLink) {
        final boolean USE_PASSWORD = Strings.isNotBlank(fileLink.getPassword());
        fileLink.setUser(this.authenticationService.getUserIfExist(authHeader));
        fileLink.setIsExpired(false);
        fileLink.setUsePassword(USE_PASSWORD);
        fileLink.setFileLink(this.getRandomFileLink());
        if (USE_PASSWORD) {
            fileLink.setPassword(this.pwdEncoder.encode(fileLink.getPassword()));
        }
        return this.repository.save(fileLink);
    }

    public List<FileLink> getAllFileLinksByAccount(String authHeader) {
        User user = this.authenticationService.getUserIfExist(authHeader);
        return user != null ? this.repository.getFileLinksByUser(user) : new ArrayList<>();
    }

    /**
     * Récupère un FileLink à partir de son lien de partage court.
     * Lève une NoSuchElementException si le lien est introuvable ou si le fichier est expiré.
     */
    public FileLink getFileLink(String fileLinkPath) {
        FileLink fileLink = this.repository.findByFileLink(fileLinkPath)
                .orElseThrow(() -> new NoSuchElementException("Lien introuvable : " + fileLinkPath));

        if (fileLink.isExpired()) {
            throw new ExpiredLinkException("Le lien a expiré : " + fileLinkPath);
        }

        return fileLink;
    }

    public String deleteFileLink(String authHeader, String fileLinkPath){
        FileLink fileLink = this.getFileLinkIfAuthorized(authHeader, fileLinkPath);
        String filePath = fileLink.getUser().getId().concat("/").concat(fileLink.getId()).concat(".").concat(fileLink.getExtension());
        this.repository.delete(fileLink);
        return filePath;
    }

    public FileLink updateFileLinkTags(String authHeader, String fileLinkPath, List<String> tags){
        FileLink fileLink = this.getFileLinkIfAuthorized(authHeader, fileLinkPath);
        fileLink.setTags(tags);
        this.repository.save(fileLink);
        return fileLink;
    }

    /**
     * Vérifie si le mot de passe fourni ne correspond pas à celui du FileLink.
     * - Si le fichier n'est pas protégé (usePassword = false), retourne false directement.
     * - Si le mot de passe est vide/null alors que le fichier est protégé, retourne true.
     * - Sinon, délègue la comparaison au PasswordEncoder.
     */
    public boolean isPasswordIncorrect(FileLink fileLink, String password) {
        if (!fileLink.getUsePassword()) {
            return false;
        }
        return Strings.isBlank(password) || !this.pwdEncoder.matches(password, fileLink.getPassword());
    }

    private String getRandomFileLink() {
        final int LINK_LENGTH = 5;
        final String CHRS = "0123456789abcdefghijklmnopqrstuvwxyz-_ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final SecureRandom SECURE_RANDOM = new SecureRandom();

        return SECURE_RANDOM.ints(LINK_LENGTH, 0, CHRS.length())
                .mapToObj(CHRS::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    private FileLink getFileLinkIfAuthorized(String authHeader, String fileLinkPath){
        User user = this.authenticationService.getUserIfExist(authHeader);
        FileLink fileLink = this.repository.findByFileLink(fileLinkPath)
                .orElseThrow(() -> new NoSuchElementException("Lien introuvable : " + fileLinkPath));
        if(user == null || !user.equals(fileLink.getUser())){
            throw new BadCredentialsException("Vous n'avez pas le droit de modifier un fichier que vous n'avez pas crée");
        }
        return fileLink;
    }
}