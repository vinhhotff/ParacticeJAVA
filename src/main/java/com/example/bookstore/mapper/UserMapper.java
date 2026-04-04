package com.example.bookstore.mapper;
import com.example.bookstore.dto.request.BookRequest;
import com.example.bookstore.dto.request.UserCreationRequest;
import com.example.bookstore.dto.response.BookResponse;
import com.example.bookstore.dto.response.UserResponse;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);
}