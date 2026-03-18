package com.openclassrooms.datashare.handler;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Hidden
@RestControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Gère les cas où un utilisateur envoi une requête qui n'est pas correcte.
     * La liste des raisons rendant cette requête incohérente sera présente dans la réponse
     * Retourne un statut 400
     */
    @Override
    public @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<FieldValidationError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldValidationError(
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getRejectedValue() == null? null : error.getRejectedValue().toString()
                ))
                .collect(Collectors.toList());

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("One or more fields have validation errors")
                .fieldErrors(fieldErrors)
                .build();

        log.warn("Validation failed: {}", fieldErrors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Gère les cas où un utilisateur n'est pas connecté et doit l'être pour accéder à la ressource
     * Retourne un statut 401
     */
    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<Object> handleBadCredentialsException(
            BadCredentialsException badCredentialsException) {
        log.warn("Bad credentials: {}", badCredentialsException.getLocalizedMessage());
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    /**
     * Gère les cas où un utilisateur essaye d'accéder à une ressource qui lui est inaccessible
     * Retourne un statut 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleForbiddenException(
            AccessDeniedException accessDeniedException) {
        log.warn("Access denied: {}", accessDeniedException.getLocalizedMessage());
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    /**
     * Gère les cas "ressource introuvable" : lien inexistant
     * Retourne un statut 404.
     */
    @ExceptionHandler(NoSuchElementException.class)
    protected ResponseEntity<Object> handleNoSuchElementException(
            NoSuchElementException noSuchElementException) {
        log.warn("Resource not found: {}", noSuchElementException.getMessage());
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Gère les cas où un lien a expiré mais existe toujours dans l'état "expiré"
     * Retourne un statut 410
     */
    @ExceptionHandler(ExpiredLinkException.class)
    protected ResponseEntity<Object> handleExpiredLinkException(
            ExpiredLinkException expiredLinkException) {
        log.warn("Link is expired: {}", expiredLinkException.getMessage());
        return new ResponseEntity<>(HttpStatus.GONE);
    }

    /**
     * Est appelée dans le cas où une erreur n'est pas gérée
     * Retourne un statut 500.
     */
    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Object> handleException(Exception exception) {
        log.warn("Unhandled error: {}", exception.getMessage());
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}