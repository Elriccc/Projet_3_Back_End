package com.openclassrooms.datashare.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String password;
    @Min(1)
    @Max(7)
    private int expirationTime;
    @NotBlank
    private MultipartFile file;
}
