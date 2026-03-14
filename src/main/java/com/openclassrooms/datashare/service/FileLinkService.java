package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.configuration.security.AuthenticationService;
import com.openclassrooms.datashare.entities.FileLink;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.repository.FileLinkRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FileLinkService {
    private final FileLinkRepository repository;
    private final PasswordEncoder pwdEncoder;
    private final AuthenticationService authenticationService;

    public FileLink saveFileLink(FileLink fileLink){
        final boolean USE_PASSWORD = Strings.isNotBlank(fileLink.getPassword());
        fileLink.setUser(this.authenticationService.getUserIfExist());
        fileLink.setIsExpired(false);
        fileLink.setUsePassword(USE_PASSWORD);
        fileLink.setFileLink(this.getRandomFileLink());
        if(USE_PASSWORD) {
            fileLink.setPassword(this.pwdEncoder.encode(fileLink.getPassword()));
        }
        return this.repository.save(fileLink);
    }

    public List<FileLink> getAllFileLinksByAccount(){

        return null;
    }

    public FileLink getFileLink(String fileLinkPath){

        return null;
    }

    public String deleteFileLink(String fileLinkPath){

        return null;
    }

    public FileLink updateFileLinkTags(String fileLinkPath, List<String> tags){

        return null;
    }

    public boolean isPasswordCorrect(FileLink fileLink, String password){

        return false;
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
}
