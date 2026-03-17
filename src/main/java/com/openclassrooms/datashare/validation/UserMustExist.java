package com.openclassrooms.datashare.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Vérifie que la combinaison login/mot de passe correspond bien à un utilisateur de la base
 */
@Documented
@Constraint(validatedBy = UserMustExistValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UserMustExist {
    String message() default "Login ou mot de passe incorrect";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String login();
    String password();
}
