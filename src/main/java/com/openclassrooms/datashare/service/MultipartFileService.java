package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.entities.FileLink;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.NoSuchElementException;
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
        File file = this.buildFilePath(fileLink, multipartFile);
        File parent = file.getParentFile();
        parent.mkdir();
        try {
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(multipartFile.getBytes());
            outputStream.close();
        } catch (IOException e) {
            throw new IOException();
        }
    }

    /**
     * Récupère le fichier physique associé au FileLink et le retourne sous forme de MultipartFile.
     * Reconstruit le chemin en utilisant l'extension stockée dans l'entité FileLink.
     * Lève une NoSuchElementException si le fichier est introuvable sur le disque.
     */
    public MultipartFile getFile(FileLink fileLink) {
        File file = this.buildFilePath(fileLink);

        if (!file.exists()) {
            throw new NoSuchElementException(
                    "Fichier physique introuvable sur le disque : " + file.getAbsolutePath());
        }

        try {
            byte[] content = Files.readAllBytes(file.toPath());
            String filename = fileLink.getName() + "." + fileLink.getExtension();
            String contentType = Files.probeContentType(file.toPath());

            return new MockMultipartFile(
                    "file",
                    filename,
                    contentType != null ? contentType : "application/octet-stream",
                    content
            );
        } catch (IOException e) {
            throw new NoSuchElementException(
                    "Impossible de lire le fichier : " + file.getAbsolutePath());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Construit le chemin du fichier lors de l'upload (le nom original du MultipartFile est utilisé
     * pour récupérer l'extension).
     */
    private @NonNull File buildFilePath(FileLink fileLink, MultipartFile multipartFile) {
        Objects.requireNonNull(multipartFile.getOriginalFilename());
        String filePath = this.DATA_PATH;
        if (fileLink.getUser() == null) {
            filePath = filePath.concat(NO_USER_DIRECTORY);
        } else {
            filePath = filePath.concat(fileLink.getUser().getId());
        }
        filePath = filePath.concat(fileLink.getId());
        filePath = filePath.concat(multipartFile.getOriginalFilename()
                .substring(multipartFile.getOriginalFilename().lastIndexOf(".")));
        return new File(filePath);
    }

    /**
     * Construit le chemin du fichier lors du téléchargement (l'extension est lue depuis l'entité
     * FileLink, qui l'a stockée à l'upload).
     */
    private @NonNull File buildFilePath(FileLink fileLink) {
        String filePath = this.DATA_PATH;
        if (fileLink.getUser() == null) {
            filePath = filePath.concat(NO_USER_DIRECTORY);
        } else {
            filePath = filePath.concat(fileLink.getUser().getId());
        }
        filePath = filePath.concat(fileLink.getId());
        filePath = filePath.concat("." + fileLink.getExtension());
        return new File(filePath);
    }
}