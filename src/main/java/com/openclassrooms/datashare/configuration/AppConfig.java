package com.openclassrooms.datashare.configuration;

import com.openclassrooms.datashare.validator.FileLinkValidator;
import com.openclassrooms.datashare.validator.UserValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
public class AppConfig {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setLocation(new FileSystemResource(".env"));
        return configurer;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) { return authConfig.getAuthenticationManager(); }

    @Bean
    public UserValidator userValidator() { return new UserValidator(); }

    @Bean
    public FileLinkValidator fileLinkValidator() { return new FileLinkValidator(); }
}
