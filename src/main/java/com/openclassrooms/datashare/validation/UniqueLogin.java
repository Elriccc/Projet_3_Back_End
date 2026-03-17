package com.openclassrooms.datashare.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Vérifie qu'un login est bien unique en base
 */
@Documented
@Constraint(validatedBy = UniqueLoginValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueLogin {
    String message() default "Un utilisateur avec ce login existe déjà";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
