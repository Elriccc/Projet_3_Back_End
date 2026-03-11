package com.openclassrooms.datashare.validation;

import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserMustExistValidator implements ConstraintValidator<UserMustExist, Object> {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    private String loginField;
    private String passwordField;

    @Override
    public void initialize(UserMustExist constraintAnnotation){
        this.loginField = constraintAnnotation.login();
        this.passwordField = constraintAnnotation.password();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object login = new BeanWrapperImpl(value).getPropertyValue(loginField);
        Object password = new BeanWrapperImpl(value).getPropertyValue(passwordField);
        if(login == null || password == null
         || StringUtils.isBlank(login.toString()) || StringUtils.isBlank(password.toString())){
            return true; //Let NotBlank handle the error
        }

        Optional<User> optionalUser = repository.findByLogin(login.toString()).stream().findFirst();
        boolean isValid = optionalUser.isPresent()
                && passwordEncoder.matches(password.toString(), optionalUser.get().getPassword());

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(loginField)
                    .addPropertyNode(passwordField)
                    .addConstraintViolation();
        }
        return isValid;
    }
}
