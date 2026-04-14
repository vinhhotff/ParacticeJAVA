package com.example.bookstore.mapper;

import com.example.bookstore.dto.request.UserCreationRequest;
import com.example.bookstore.dto.response.UserResponse;
import com.example.bookstore.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",uses = {RoleMapper.class})
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);
}