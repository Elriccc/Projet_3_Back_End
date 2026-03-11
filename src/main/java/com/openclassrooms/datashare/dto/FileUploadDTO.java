package com.openclassrooms.datashare.dto;

import com.openclassrooms.datashare.validation.FilePassword;
import com.openclassrooms.datashare.validation.MultipartFileIsCorrect;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadDTO {
    @NotNull(message = "Le fichier est requis")
    @MultipartFileIsCorrect
    private MultipartFile file;
    @FilePassword
    private String password;
    @Min(value = 1, message = "Le fichier a minimum un jour avant expiration")
    @Max(value = 7, message = "Le fichier a maximum une semaine avant expiration")
    private int expirationTime;
}
