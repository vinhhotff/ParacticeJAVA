package com.example.bookstore.mapper;


import com.example.bookstore.dto.request.PermissionRequest;
import com.example.bookstore.dto.response.PermissionResponse;
import com.example.bookstore.model.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}

