package com.openclassrooms.datashare.mapper;

import ch.qos.logback.core.util.StringUtil;
import com.openclassrooms.datashare.dto.FileDTO;
import com.openclassrooms.datashare.dto.FileUploadDTO;
import com.openclassrooms.datashare.entities.FileLink;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FileDtoMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fileLink", ignore = true)
    @Mapping(target = "created_at", ignore = true)
    @Mapping(target = "updated_at", ignore = true)
    @Mapping(target = "name", expression = "java(mapName(fileUploadDTO))")
    @Mapping(target = "extension", expression = "java(mapExtension(fileUploadDTO))")
    @Mapping(target = "size", expression = "java(mapSize(fileUploadDTO))")
    @Mapping(target = "usePassword", expression = "java(mapUsePassword(fileUploadDTO))")
    @Mapping(target = "expirationDate", expression = "java(mapExpirationDate(fileUploadDTO))")
    @Mapping(target = "isExpired", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "user", ignore = true)
    FileLink toEntity(FileUploadDTO fileUploadDTO);

    default String mapName(FileUploadDTO fileUploadDTO){
        return fileUploadDTO.getFile() != null
                && fileUploadDTO.getFile().getOriginalFilename() != null
                && fileUploadDTO.getFile().getOriginalFilename().contains(".")
                    ? fileUploadDTO.getFile().getOriginalFilename().substring(0, fileUploadDTO.getFile().getOriginalFilename().lastIndexOf("."))
                    : null;
    }

    default String mapExtension(FileUploadDTO fileUploadDTO){
        return fileUploadDTO.getFile() != null
                && fileUploadDTO.getFile().getOriginalFilename() != null
                && fileUploadDTO.getFile().getOriginalFilename().contains(".")
                    ? fileUploadDTO.getFile().getOriginalFilename().substring(fileUploadDTO.getFile().getOriginalFilename().lastIndexOf(".") + 1)
                    : null;
    }

    default long mapSize(FileUploadDTO fileUploadDTO){
        return fileUploadDTO.getFile() != null? fileUploadDTO.getFile().getSize() : 0;
    }

    default boolean mapUsePassword(FileUploadDTO fileUploadDTO){
        return !StringUtil.isNullOrEmpty(fileUploadDTO.getPassword());
    }

    default LocalDate mapExpirationDate(FileUploadDTO fileUploadDTO){
        return fileUploadDTO.getExpirationTime() > 0 && fileUploadDTO.getExpirationTime() < 7? LocalDate.now().plusDays(fileUploadDTO.getExpirationTime()) : null;
    }

    @Mapping(target = "daysUntilExpired", expression = "java(mapDaysUntilExpired(fileLink))")
    FileDTO toDTO(FileLink fileLink);

    default int mapDaysUntilExpired(FileLink fileLink){
        if(fileLink.getExpirationDate() != null && fileLink.getExpirationDate().isAfter(LocalDate.now())) {
            return (int) ChronoUnit.DAYS.between(LocalDate.now(), fileLink.getExpirationDate());
        }
        return 0;
    }
}
