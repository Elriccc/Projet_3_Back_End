package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.entities.FileLink;
import com.openclassrooms.datashare.repository.FileLinkRepository;
import com.openclassrooms.datashare.repository.UserRepository;
import com.openclassrooms.datashare.validator.FileLinkValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FileLinkService {
    private final FileLinkValidator validator;
    private final FileLinkRepository repository;
    private final UserRepository userRepository;

    public void saveFileLink(FileLink fileLink){


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
