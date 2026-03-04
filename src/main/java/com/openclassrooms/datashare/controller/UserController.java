package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.dto.UserDTO;
import com.openclassrooms.datashare.mapper.UserDtoMapper;
import com.openclassrooms.datashare.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "UserController", description = "Endpoints permettant de contrôler l'authentification")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {
    private final UserService service;
    private final UserDtoMapper mapper;

    @Operation(method = "register", summary = "Enregistrer un utilisateur", description = "Enregistrer un nouvel utilisateur en fournissant son login et son mot de passe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Compte crée"),
            @ApiResponse(responseCode = "400", description = "Requête incorrecte")
    })
    @PostMapping("/api/register")
    public ResponseEntity<?> register(@RequestBody UserDTO userDto) {
        service.register(mapper.toEntity(userDto));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(method = "login", summary = "Se connecter", description = "Se connecter en fournissant un login et un mot de passe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connexion réussie", content = {
                    @Content(mediaType = MediaType.TEXT_PLAIN_VALUE
                            , schema = @Schema(implementation = String.class)
                            , examples = @ExampleObject(value = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30"))
            }),
            @ApiResponse(responseCode = "400", description = "Requête incorrecte")
    })
    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDto) {
        String jwt = service.login(userDto.getLogin(), userDto.getPassword());
        return ResponseEntity.ok(jwt);
    }

    @Operation(method = "isAuthTokenCorrect", summary = "Récupérer la validité d'un token", description = "Obtenir la validité d'un token pour savoir s'il est correct et non expiré")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validité récupérée", content = {
                    @Content(mediaType = MediaType.TEXT_PLAIN_VALUE
                            , schema = @Schema(implementation = Boolean.class)
                            , examples = @ExampleObject(value = "false"))}),
    })
    @GetMapping("/api/auth/{token}")
    public ResponseEntity<?> isAuthTokenCorrect(@PathVariable String token){
        return ResponseEntity.ok(service.validateToken(token));
    }
}
