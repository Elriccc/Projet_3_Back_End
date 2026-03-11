package com.openclassrooms.datashare.dto;

import com.openclassrooms.datashare.validation.UniqueLogin;
import com.openclassrooms.datashare.validation.UserMustExist;
import com.openclassrooms.datashare.validation.UserValidationGroups;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@UserMustExist(groups = UserValidationGroups.Login.class, login = "login", password = "password")
public class UserDTO {
    @NotBlank(message = "Le login ne peut pas être vide")
    @Email(regexp = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", message = "Le login n'est pas un email")
    @UniqueLogin(groups = UserValidationGroups.Register.class)
    private String login;
    @NotBlank(message = "Le mot de passe ne peut pas être vide")
    @Size(min = 8, message = "Le mot de passe doit faire au minimum 8 caractères")
    private String password;
}
