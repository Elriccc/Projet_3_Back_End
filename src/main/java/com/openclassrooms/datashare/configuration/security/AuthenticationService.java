package com.openclassrooms.datashare.configuration.security;

import com.openclassrooms.datashare.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    public Authentication getAuthentication(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public boolean isAuthenticate(){
        Authentication authentication = this.getAuthentication();
        return authentication != null && authentication.getPrincipal() != null
                && !"anonymousUser".equals(authentication.getPrincipal().toString());
    }

    public User getUserIfExist(){
        Authentication authentication = this.getAuthentication();
        return (this.isAuthenticate())? (User) authentication.getPrincipal() : null;
    }
}
