package com.openclassrooms.datashare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDTO {
    @NotBlank
    @Email(regexp = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", message = "Le login n'est pas un email")
    private String login;
    @NotBlank
    @Min(value = 8, message = "Le mot de passe doit faire au minimum 8 caractères")
    private String password;
}
