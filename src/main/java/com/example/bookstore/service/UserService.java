package com.example.bookstore.service;



import com.example.bookstore.dto.request.UserCreationRequest;
import com.example.bookstore.dto.response.UserResponse;

public interface UserService {
    UserResponse createUser(UserCreationRequest request);
    UserResponse getMyinfo();
}