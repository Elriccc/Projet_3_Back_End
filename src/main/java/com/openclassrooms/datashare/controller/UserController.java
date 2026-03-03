package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.dto.UserDTO;
import com.openclassrooms.datashare.mapper.UserDtoMapper;
import com.openclassrooms.datashare.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {
    private final UserService service;
    private final UserDtoMapper mapper;

    @PostMapping("/api/register")
    public ResponseEntity<?> register(@RequestBody UserDTO userDto) {
        service.register(mapper.toEntity(userDto));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDto) {
        String jwt = service.login(userDto.getLogin(), userDto.getPassword());
        return ResponseEntity.ok(jwt);
    }

    @GetMapping("/api/auth/{token}")
    public ResponseEntity<?> isAuthTokenCorrect(@PathVariable String token){
        return ResponseEntity.ok(service.validateToken(token));
    }
}
