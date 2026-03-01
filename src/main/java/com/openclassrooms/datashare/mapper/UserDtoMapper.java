package com.openclassrooms.datashare.mapper;

import com.openclassrooms.datashare.dto.UserDTO;
import com.openclassrooms.datashare.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserDtoMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created_at", ignore = true)
    @Mapping(target = "updated_at", ignore = true)
    @Mapping(target = "fileLinks", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User toEntity(UserDTO userDTO);
    UserDTO toDTO(User user);
}
