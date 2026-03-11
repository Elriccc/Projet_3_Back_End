package com.openclassrooms.datashare.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class FileDTO {
    private String fileLink;
    private String name;
    private String extension;
    private long size;
    private boolean usePassword;
    private int daysUntilExpired;
    private List<String> tags;
    private MultipartFile file;
}
