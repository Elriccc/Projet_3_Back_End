package com.openclassrooms.datashare.configuration.security;

import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final CustomJwtService jwtService;
    private final UserRepository userRepository;

    public User getUserIfExist(String authHeader){
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return null;
        }
        String jwt = authHeader.substring(7);
        String username = jwtService.getUsernameFromToken(jwt);
        return userRepository.findByLogin(username).orElse(null);
    }
}
