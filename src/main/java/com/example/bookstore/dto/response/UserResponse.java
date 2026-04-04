package com.example.bookstore.dto.response;

import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class UserResponse {
    String id;
    String username;
    String firstName;
    String lastName;
    Set<String> roles;
}
