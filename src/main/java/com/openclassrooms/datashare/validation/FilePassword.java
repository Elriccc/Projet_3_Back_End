package com.openclassrooms.datashare.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FilePasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FilePassword {
    String message() default "Le mot de passe doit être de minimum 6 caractères";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
