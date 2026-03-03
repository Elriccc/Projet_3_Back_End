package com.openclassrooms.datashare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class FileLinkReadDTO {
    @NotBlank
    private String name;
    private int daysUntilExpired;
    private List<String> tags;
}
