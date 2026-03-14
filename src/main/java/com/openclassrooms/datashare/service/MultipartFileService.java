package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.entities.FileLink;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MultipartFileService {
    @Value("${app.data-path}")
    private String DATA_PATH;
    @Value("${app.no-user-directory}")
    private String NO_USER_DIRECTORY;

    public void addFile(FileLink fileLink, MultipartFile multipartFile) throws IOException {
        File file = this.getFile(fileLink, multipartFile);
        File parent = file.getParentFile();
        parent.mkdir();
        try {
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(multipartFile.getBytes());
            outputStream.close();
        } catch(IOException e){
            throw new IOException();
        }

    }

    private @NonNull File getFile(FileLink fileLink, MultipartFile multipartFile) {
        Objects.requireNonNull(multipartFile.getOriginalFilename());
        String filePath = this.DATA_PATH;
        if(fileLink.getUser() == null){
            filePath = filePath.concat(NO_USER_DIRECTORY);
        } else {
            filePath = filePath.concat(fileLink.getUser().getId());
        }
        filePath = filePath.concat(fileLink.getId());
        filePath = filePath.concat(multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf(".")));
        return new File(filePath);
    }

    public MultipartFile getFile(FileLink fileLink){

        return null;
    }
}
