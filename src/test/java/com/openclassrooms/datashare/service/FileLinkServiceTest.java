package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.configuration.security.AuthenticationService;
import com.openclassrooms.datashare.repository.FileLinkRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class FileLinkServiceTest {

    @Mock
    private FileLinkRepository repository;
    @Mock
    private PasswordEncoder pwdEncoder;
    @Mock
    private AuthenticationService authenticationService;
    @InjectMocks
    private FileLinkService service;

}
