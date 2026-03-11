package com.openclassrooms.datashare.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MultipartFileValidator implements ConstraintValidator<MultipartFileIsCorrect, MultipartFile> {
    private final List<String> AUTHORIZED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif"
            ,"doc", "docx", "dotx", "xltx", "ppt", "pptx", "potx", "ppsx", "pdf", "html", "pages"
            , "xls", "xlsx", "xml", "zip", "csv", "txt"
            , "mp3", "mp4", "midi");

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        if(multipartFile == null){
            return true; //Let NotNull handle the error
        }
        if(multipartFile.getOriginalFilename() == null){
            this.customMessageForValidation(constraintValidatorContext
                    , "Le fichier doit avoir un nom et une extension");
            return false;
        }
        boolean isValid = true;
        final String FILE_NAME_WITH_EXTENSION = multipartFile.getOriginalFilename();
        if(FILE_NAME_WITH_EXTENSION.length() > 255) {
            this.customMessageForValidation(constraintValidatorContext
                    , "Le nom du fichier avec son extension ne doit pas dépasser 255 caractères");
            isValid = false;
        }
        final String EXTENSION = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf(".") + 1);
        if(!AUTHORIZED_EXTENSIONS.contains(EXTENSION)){
            this.customMessageForValidation(constraintValidatorContext
                    , "L'extension du fichier n'est pas autorisé, réessayez en compressant en .zip le fichier");
            isValid = false;
        }
        final long SIZE = multipartFile.getSize();
        final long KILO_BYTE = 1000;
        final long GIGA_BYTE = 1000*1000*1000;
        if(SIZE < KILO_BYTE){
            this.customMessageForValidation(constraintValidatorContext
                    , "Le fichier doit faire au moins 1Ko");
            isValid = false;
        } else if(SIZE > GIGA_BYTE) {
            this.customMessageForValidation(constraintValidatorContext
                    , "Le fichier ne peut pas faire plus de 1Go");
            isValid = false;
        }
        return isValid;
    }

    private void customMessageForValidation(ConstraintValidatorContext constraintContext, String message) {
        constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
