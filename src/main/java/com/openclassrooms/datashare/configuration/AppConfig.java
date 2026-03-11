package com.openclassrooms.datashare.configuration;

import com.openclassrooms.datashare.validator.FileLinkValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
public class AppConfig {
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) { return authConfig.getAuthenticationManager(); }
}
