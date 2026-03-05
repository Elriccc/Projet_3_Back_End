package com.openclassrooms.datashare.mapper;

import ch.qos.logback.core.util.StringUtil;
import com.openclassrooms.datashare.dto.FileLinkReadDTO;
import com.openclassrooms.datashare.dto.FileLinkUploadDTO;
import com.openclassrooms.datashare.entities.FileLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FileLinkDtoMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fileLink", ignore = true)
    @Mapping(target = "created_at", ignore = true)
    @Mapping(target = "updated_at", ignore = true)
    @Mapping(target = "usePassword", expression = "java(mapUsePassword(fileLinkUploadDTO))")
    @Mapping(target = "expirationDate", expression = "java(mapExpirationDate(fileLinkUploadDTO))")
    @Mapping(target = "isExpired", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "user", ignore = true)
    FileLink toEntity(FileLinkUploadDTO fileLinkUploadDTO);

    default boolean mapUsePassword(FileLinkUploadDTO fileLinkUploadDTO){
        return !StringUtil.isNullOrEmpty(fileLinkUploadDTO.getPassword());
    }

    default LocalDate mapExpirationDate(FileLinkUploadDTO fileLinkUploadDTO){
        return LocalDate.now().plusDays(fileLinkUploadDTO.getExpirationTime());
    }

    @Mapping(target = "file", ignore = true)
    @Mapping(target = "daysUntilExpired", expression = "java(mapDaysUntilExpired(fileLink))")
    FileLinkReadDTO toDTO(FileLink fileLink);

    default int mapDaysUntilExpired(FileLink fileLink){
        if(fileLink.getExpirationDate() != null && fileLink.getExpirationDate().isAfter(LocalDate.now())) {
            return (int) ChronoUnit.DAYS.between(fileLink.getExpirationDate(), LocalDate.now());
        }
        return 0;
    }
}
