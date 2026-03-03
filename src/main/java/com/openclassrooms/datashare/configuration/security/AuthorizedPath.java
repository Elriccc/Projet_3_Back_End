package com.openclassrooms.datashare.configuration.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthorizedPath {
    @Value("${springdoc.api-docs.path}")
    private String API_DOCS_PATH;
    @Value("${springdoc.swagger-ui.path}")
    private String SWAGGER_UI_PATH;

    public String[] getPermitAllPaths(){
        return new String[]{"/actuator/**", "/api/register", "/api/login", "/api/auth/**"
                , API_DOCS_PATH, API_DOCS_PATH+"/**"
                , SWAGGER_UI_PATH+"/**"};
    }
}
