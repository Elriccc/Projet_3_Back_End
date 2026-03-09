package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.configuration.security.AuthenticationService;
import com.openclassrooms.datashare.entities.FileLink;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.repository.FileLinkRepository;
import com.openclassrooms.datashare.repository.UserRepository;
import com.openclassrooms.datashare.validator.FileLinkValidator;
import io.jsonwebtoken.lang.Assert;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FileLinkService {
    private final FileLinkValidator validator;
    private final FileLinkRepository repository;
    private final UserRepository userRepository;
    private final PasswordEncoder pwdEncoder;
    private final AuthenticationService authenticationService;

    public FileLink saveFileLink(FileLink fileLink){
        this.validator.validate(fileLink);
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
}
