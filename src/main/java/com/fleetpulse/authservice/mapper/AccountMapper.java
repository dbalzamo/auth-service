package com.fleetpulse.authservice.mapper;

import com.fleetpulse.authservice.dto.request.RegisterRequest;
import com.fleetpulse.authservice.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AccountMapper {

    @Mapping(target="id", ignore = true)
    @Mapping(target="password", ignore = true)
    @Mapping(target="enabled", ignore = true)
    @Mapping(target="role", ignore = true)
    @Mapping(target="refreshTokens", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target="createdAt", ignore = true)
    @Mapping(target="updatedAt", ignore = true)
    @Mapping(target="deletedAt", ignore = true)
    Account toEntity(RegisterRequest request);
}