package com.openclassrooms.datashare.validator;

import org.jspecify.annotations.NonNull;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class FileLinkValidator implements Validator {
    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {

    }
}
