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
        User user = (User) target;
        final String login = user.getLogin();
        final String password = user.getPassword();
        if(!Strings.isEmpty(login) && !login.matches("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")){
            errors.rejectValue("login", "login.badformat", "Login is not a valid email");
        }
        if(!Strings.isEmpty(password) && password.length() < 8) {
            errors.rejectValue("password", "password.tooShort", "Password is too short (minimum 8 characters)");
        }
        if (errors.hasErrors()) {
            throw new IllegalArgumentException(errors.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining("</br>-", "-", "")));
        }
    }
}
