package com.openclassrooms.datashare.validator;

import com.openclassrooms.datashare.entities.User;
import org.apache.logging.log4j.util.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import java.util.stream.Collectors;

public class UserValidator implements Validator {
    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return false;
    }

    public void validate(User user){
        Errors errors = new BeanPropertyBindingResult(user, "user");
        this.validate(user, errors);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {

    }
}
