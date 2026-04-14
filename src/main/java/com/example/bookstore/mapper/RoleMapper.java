package com.example.bookstore.mapper;

import com.example.bookstore.dto.request.RoleRequest;
import com.example.bookstore.dto.response.RoleResponse;
import com.example.bookstore.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    // Bỏ qua cái list Permission khi copy sang, ta sẽ gán bằng tay ở Service
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
