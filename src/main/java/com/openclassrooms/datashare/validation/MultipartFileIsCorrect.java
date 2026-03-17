package com.openclassrooms.datashare.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Valide qu'un MultipartFile a les bonnes propriétés pour être téléverser sur le serveur, pour cela il faut:
 * - Qu'il ait un nom de fichier et une extension
 * - Que le nom de fichier + extension fasse maximum 255 caractères
 * - Que l'extension fasse partie de la liste autorisée par le serveur
 * - Qu'il fasse entre 1Ko et 1Go
 */
@Documented
@Constraint(validatedBy = MultipartFileValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MultipartFileIsCorrect {
    String message() default "default message";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
