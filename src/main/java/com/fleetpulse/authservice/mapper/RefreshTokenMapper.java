package com.fleetpulse.authservice.mapper;

import com.fleetpulse.authservice.dto.request.RefreshRequest;
import com.fleetpulse.authservice.model.RefreshToken;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {

    RefreshToken toEntity(RefreshRequest refreshRequest);
}