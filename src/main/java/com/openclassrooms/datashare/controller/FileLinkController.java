package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.dto.FileLinkReadDTO;
import com.openclassrooms.datashare.dto.FileLinkUploadDTO;
import com.openclassrooms.datashare.entities.FileLink;
import com.openclassrooms.datashare.mapper.FileLinkDtoMapper;
import com.openclassrooms.datashare.service.FileLinkService;
import com.openclassrooms.datashare.service.MultipartFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "FileLinkController", description = "Endpoints permettant d'opérer sur les fichiers")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class FileLinkController {
    private final FileLinkService service;
    private final MultipartFileService fileService;
    private final FileLinkDtoMapper mapper;

    @Operation(method = "addFiles", summary = "Ajouter un fichier", description = "Ajouter un fichier avec un temps d'expiration et optionnellement un mot de passe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Fichier ajouté"),
            @ApiResponse(responseCode = "400", description = "Requête incorrecte")
    })
    @PostMapping("/api/files")
    public ResponseEntity<?> addFile(@RequestBody FileLinkUploadDTO fileLinkUploadDTO){
        this.service.saveFileLink(this.mapper.toEntity(fileLinkUploadDTO));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(method = "retrieveAllFiles", summary = "Récupérer tous les fichiers", description = "Récupérer toutes les mêta-données des fichiers d'un utilisateur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fichiers récupérés", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE
                , array = @ArraySchema(schema = @Schema(implementation = FileLinkReadDTO.class))
                , examples = @ExampleObject(value =
                    "[" +
                        "{" +
                            "fileLink: 'lG4Ef88e8CEr8gZ0'," +
                            "name: 'myAwesomeDatas.zip'," +
                            "daysUntilExpired: 3," +
                            "tags: ['new', 'awesome', 'zip']" +
                        "}," +
                        "{" +
                            "fileLink: 'lG4Ef88e8CEv8gZ0'," +
                            "name: 'myAwesomeImage.png'," +
                            "daysUntilExpired: 1," +
                            "tags: ['awesome', 'image']" +
                        "}," +
                    "]"
                )
            )
        }),
    })
    @GetMapping("/api/files")
    public ResponseEntity<?> retrieveAllFiles(){
        List<FileLinkReadDTO> DTOs = this.service.getAllFileLinksByAccount().stream().map(this.mapper::toDTO).toList();
        return ResponseEntity.ok(DTOs);
    }

    @Operation(method = "downloadFiles", summary = "Télécharger un fichier", description = "Télécharger un fichier et récupérer ses mêta-données")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fichier correctement téléchargé", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE
                , schema = @Schema(implementation = FileLinkReadDTO.class)
                , examples = @ExampleObject(value =
                    "{" +
                        "fileLink: 'lG4Ef88e8CEv8gZ0'," +
                        "name: 'myAwesomeImage.png'," +
                        "daysUntilExpired: 1," +
                        "tags: ['awesome', 'image']" +
                    "}")
                )
        }),
            @ApiResponse(responseCode = "400", description = "Requête incorrecte")
    })
    @GetMapping("/api/files/download/{fileLinkPath}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileLinkPath, @RequestBody String password){
        FileLink fileLink = this.service.getFileLink(fileLinkPath);
        FileLinkReadDTO dto = this.mapper.toDTO(fileLink);
        if(this.service.isPasswordCorrect(fileLink, password)) {
            MultipartFile file = this.fileService.getFile(fileLink);
            dto.setFile(file);
        }
        return ResponseEntity.ok(dto);
    }

    @Operation(method = "deleteFile", summary = "Supprimer un fichier", description = "Supprimer un fichier et ses mêta-données")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fichier correctement supprimé", content = {
            @Content(mediaType = MediaType.TEXT_PLAIN_VALUE
                    , schema = @Schema(implementation = String.class)
                    , examples = @ExampleObject(value = "lG4Ef88e8CEv8gZ0"))
        }),
    })
    @DeleteMapping("/api/files/{fileLinkPath}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileLinkPath){
        String dbFileId = this.service.deleteFileLink(fileLinkPath);
        return ResponseEntity.ok(dbFileId);
    }

    @Operation(method = "updateTags", summary = "Mettre à jour les tags d'un fichier", description = "Mettre à jour les tags d'un fichier et récupérer ses mêta-données")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tags mis à jour"
            , content = {
                @Content(mediaType = MediaType.APPLICATION_JSON_VALUE
                    , schema = @Schema(implementation = FileLinkReadDTO.class)
                    , examples = @ExampleObject(value =
                    "{" +
                        "fileLink: 'lG4Ef88e8CEv8gZ0'," +
                        "name: 'myAwesomeImage.png'," +
                        "daysUntilExpired: 1," +
                        "tags: ['awesome', 'image']" +
                    "}")
                )
            }
        )
    })
    @PutMapping("/api/files/{fileLinkPath}")
    public ResponseEntity<?> updateTags(@PathVariable String fileLinkPath, @RequestBody List<String> tags){
        FileLink fileLink = this.service.updateFileLinkTags(fileLinkPath, tags);
        return ResponseEntity.ok(this.mapper.toDTO(fileLink));
    }
}
