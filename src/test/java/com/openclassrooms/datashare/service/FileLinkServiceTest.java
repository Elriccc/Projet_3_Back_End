package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.repository.FileLinkRepository;
import com.openclassrooms.datashare.repository.UserRepository;
import com.openclassrooms.datashare.validator.FileLinkValidator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FileLinkServiceTest {

    @Mock
    private FileLinkValidator validator;
    @Mock
    private FileLinkRepository repository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private FileLinkService service;

}
