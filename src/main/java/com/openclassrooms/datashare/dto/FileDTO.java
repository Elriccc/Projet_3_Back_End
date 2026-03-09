package com.openclassrooms.datashare.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class FileDTO {
    @NotBlank
    private String fileLink;
    @NotBlank
    private String name;
    private boolean usePassword;
    @Min(0)
    private int daysUntilExpired;
    private List<String> tags;
    private MultipartFile file;
}
