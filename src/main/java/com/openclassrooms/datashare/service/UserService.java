package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.configuration.security.CustomJwtService;
import com.openclassrooms.datashare.configuration.security.CustomUserDetailService;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.repository.UserRepository;
import com.openclassrooms.datashare.validator.UserValidator;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserValidator validator;
    private final UserRepository repository;
    private final AuthenticationProvider authenticationManager;
    private final CustomUserDetailService userDetailService;
    private final PasswordEncoder passwordEncoder;
    private final CustomJwtService jwtService;

    public void register(User entity) {
    }

    public String login(@NotBlank String login, @NotBlank String password) {
        return null;
    }
}
