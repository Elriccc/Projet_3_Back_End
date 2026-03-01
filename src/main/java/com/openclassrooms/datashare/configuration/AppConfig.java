package com.openclassrooms.datashare.configuration;

import com.openclassrooms.datashare.validator.FileLinkValidator;
import com.openclassrooms.datashare.validator.UserValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public UserValidator userValidator() { return new UserValidator(); }

    @Bean
    public FileLinkValidator fileLinkValidator() { return new FileLinkValidator(); }
}
