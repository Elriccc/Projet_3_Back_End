package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.dto.UserDTO;
import com.openclassrooms.datashare.mapper.UserDtoMapper;
import com.openclassrooms.datashare.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {

    private UserService service;
    private UserDtoMapper mapper;

    @PostMapping("/api/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDto) {
        service.register(mapper.toEntity(userDto));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(UserDTO userDto) {
        String jwt = service.login(userDto.getLogin(), userDto.getPassword());
        
        return ResponseEntity.ok(jwt);
    }
}
