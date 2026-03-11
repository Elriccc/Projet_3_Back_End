package com.openclassrooms.datashare.validation;

import com.openclassrooms.datashare.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueLoginValidator implements ConstraintValidator<UniqueLogin, String> {
    private final UserRepository repository;

    @Override
    public boolean isValid(String login, ConstraintValidatorContext constraintValidatorContext) {
        if(StringUtils.isBlank(login)) return true; //Let NotBlank handle the error
        return !repository.existsByLogin(login);
    }
}
