package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.entities.FileLink;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MultipartFileService {

    public void addFile(MultipartFile file){

    }

    public MultipartFile getFile(FileLink fileLink){

        return null;
    }
}
