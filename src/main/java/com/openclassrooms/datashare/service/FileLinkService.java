package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.repository.FileLinkRepository;
import com.openclassrooms.datashare.validator.FileLinkValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FileLinkService {
    private FileLinkValidator validator;
    private FileLinkRepository repository;
}
