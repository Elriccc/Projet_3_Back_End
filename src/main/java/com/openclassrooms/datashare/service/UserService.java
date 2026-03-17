package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.configuration.security.CustomJwtService;
import com.openclassrooms.datashare.configuration.security.CustomUserDetailService;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final AuthenticationProvider authenticationManager;
    private final CustomUserDetailService userDetailService;
    private final PasswordEncoder passwordEncoder;
    private final CustomJwtService jwtService;

    /**
     * Créer un nouveau compte en base
     */
    public void register(User user) {
        log.info("Registering new user");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repository.save(user);
    }

    /**
     * Créer la connection d'un utilisateur et renvoie le token qui lui a été généré
     */
    public String login(String login, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, password));
        return jwtService.generateToken(userDetailService.loadUserByUsername(login));
    }

    /**
     * Renvoie la validation qu'un token existe et n'a pas expiré
     */
    public boolean validateToken(@NotBlank String token){
        return !jwtService.isTokenExpired(token);
    }
}
