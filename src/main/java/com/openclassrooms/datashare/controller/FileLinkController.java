package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.mapper.FileLinkDtoMapper;
import com.openclassrooms.datashare.service.FileLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class FileLinkController {

    private FileLinkService service;
    private FileLinkDtoMapper mapper;

    @PostMapping("/api/files")
    public ResponseEntity<?> addFile(){

        return null;
    }

    @GetMapping("/api/files")
    public ResponseEntity<?> retrieveAllFiles(){

        return null;
    }

    @GetMapping("/api/files/{filesLink}")
    public ResponseEntity<?> downloadFile(@PathVariable String filesLink){

        return null;
    }

    @DeleteMapping("/api/files/{filesLink}")
    public ResponseEntity<?> deleteFile(@PathVariable String filesLink){

        return null;
    }

    @PutMapping("/api/files/{filesLink}")
    public ResponseEntity<?> updateTags(@PathVariable String filesLink){

        return null;
    }
}
