package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.dto.FileDTO;
import com.openclassrooms.datashare.dto.FileUploadDTO;
import com.openclassrooms.datashare.entities.FileLink;
import com.openclassrooms.datashare.mapper.FileDtoMapper;
import com.openclassrooms.datashare.service.FileLinkService;
import com.openclassrooms.datashare.service.MultipartFileService;
import com.openclassrooms.datashare.validation.FilePassword;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "FileLinkController", description = "Endpoints permettant d'opérer sur les fichiers")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class FileController {
    private final FileLinkService service;
    private final MultipartFileService fileService;
    private final FileDtoMapper mapper;

    @Operation(method = "addFiles", summary = "Ajouter un fichier", description = "Ajouter un fichier avec un temps d'expiration et optionnellement un mot de passe")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Fichier ajouté", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE
            , schema = @Schema(implementation = FileDTO.class)
            , examples = @ExampleObject(value =
                "{" +
                    "fileLink: 'lG4Eh', " +
                    "name: 'myAwesomeImage', " +
                    "extension: 'png', " +
                    "size: 15000, " +
                    "daysUntilExpired: 1, " +
                    "tags: ['awesome', 'image'], " +
                    "usePassword: false" +
                "}")
            )
        }),
        @ApiResponse(responseCode = "400", description = "Requête incorrecte")
    })
    @PostMapping("/api/files")
    public ResponseEntity<?> addFile(@Validated @ModelAttribute FileUploadDTO fileUploadDTO){
        FileLink fileLink = this.service.saveFileLink(this.mapper.toEntity(fileUploadDTO));
        this.fileService.addFile(fileLink, fileUploadDTO.getFile());
        return new ResponseEntity<>(this.mapper.toDTO(fileLink), HttpStatus.CREATED);
    }

    @Operation(method = "retrieveAllFiles", summary = "Récupérer tous les fichiers", description = "Récupérer toutes les mêta-données des fichiers d'un utilisateur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fichiers récupérés", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE
                , array = @ArraySchema(schema = @Schema(implementation = FileDTO.class))
                , examples = @ExampleObject(value =
                    "[" +
                        "{" +
                            "fileLink: 'lG4Ef', " +
                            "name: 'myAwesomeDatas', " +
                            "extension: 'zip', " +
                            "size: 180000, " +
                            "daysUntilExpired: 3, " +
                            "tags: ['new', 'awesome', 'zip'], " +
                            "usePassword: true" +
                        "}, " +
                        "{" +
                            "fileLink: '88e8p', " +
                            "name: 'myAwesomeImage', " +
                            "extension: 'png', " +
                            "size: 15000, " +
                            "daysUntilExpired: 1, " +
                            "tags: ['awesome', 'image'], " +
                            "usePassword: false" +
                        "}," +
                    "]"
                )
            )
        }),
    })
    @GetMapping("/api/files")
    public ResponseEntity<?> retrieveAllFiles(){
        List<FileDTO> DTOs = this.service.getAllFileLinksByAccount().stream().map(this.mapper::toDTO).toList();
        return ResponseEntity.ok(DTOs);
    }

    @Operation(method = "retrieveFileByLink", summary = "Récupérer un fichier selon son lien", description = "Récupérer les informations d'un fichier à partir de son lien")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fichier récupéré", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE
                , schema = @Schema(implementation = FileDTO.class)
                , examples = @ExampleObject(value =
                "{" +
                    "fileLink: 'lG4Ef', " +
                    "name: 'myAwesomeDatas', " +
                    "extension: 'zip', " +
                    "size: 180000, " +
                    "daysUntilExpired: 3, " +
                    "tags: ['new', 'awesome', 'zip'], " +
                    "usePassword: true" +
                "}"
                )
            )
        }),
    })
    @GetMapping("/api/files/{fileLinkPath}")
    public ResponseEntity<?> retrieveFileByLink(@PathVariable String fileLinkPath){
        FileLink fileLink = this.service.getFileLink(fileLinkPath);
        FileDTO dto = this.mapper.toDTO(fileLink);
        return ResponseEntity.ok(dto);
    }

    @Operation(method = "downloadFiles", summary = "Télécharger un fichier", description = "Télécharger un fichier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fichier correctement téléchargé"),
        @ApiResponse(responseCode = "400", description = "Requête incorrecte")
    })
    @PostMapping("/api/files/download/{fileLinkPath}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileLinkPath, @RequestBody(required = false) @FilePassword String password){
        FileLink fileLink = this.service.getFileLink(fileLinkPath);
        if(this.service.isPasswordIncorrect(fileLink, password)) {
            return ResponseEntity.badRequest().body("Le mot de passe est incorrect");
        }
        InputStreamResource resource = this.fileService.getFileStream(fileLink);

        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
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
                    , schema = @Schema(implementation = FileDTO.class)
                    , examples = @ExampleObject(value =
                    "{" +
                        "fileLink: 'lG4Ef', " +
                        "name: 'myAwesomeImage', " +
                        "extension: 'png', " +
                        "size: 15000, " +
                        "daysUntilExpired: 1, " +
                        "tags: ['awesome', 'image'], " +
                        "usePassword: false" +
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
