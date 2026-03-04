package com.openclassrooms.datashare.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class FileLinkReadDTO {
    @NotBlank
    private String id;
    @NotBlank
    private String name;
    @Min(0)
    private int daysUntilExpired;
    private List<String> tags;
    private MultipartFile file;
}
