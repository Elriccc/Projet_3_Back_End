package com.openclassrooms.datashare.validator;

import com.openclassrooms.datashare.entities.FileLink;
import org.jspecify.annotations.NonNull;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import java.util.stream.Collectors;

public class FileLinkValidator implements Validator {
    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return false;
    }

    public void validate(FileLink fileLink){
        Errors errors = new BeanPropertyBindingResult(fileLink, "fileLink");
        this.validate(fileLink, errors);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        FileLink fileLink = (FileLink) target;
        final String name = fileLink.getName();
        final boolean usePassword = fileLink.getUsePassword();
        final String password = fileLink.getPassword();

        if(name.length() > 255) {
            errors.rejectValue("name", "name.toolong", "File name and its extension is too long, must be less than 255 characters");
        }

        if(name.matches("^[a-zA-Z0-9]{1,200}\\.[a-zA-Z0-9]{1,10}$")){
            errors.rejectValue("name", "name.badformat", "Name is not a file name with its extension");
        }

        if(usePassword && password.length() < 6) {
            errors.rejectValue("password", "password.tooshort", "Password is too short (minimum 6 characters)");
        }

        if (errors.hasErrors()) {
            throw new IllegalArgumentException(errors.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining("</br>-", "-", "")));
        }
    }
}
