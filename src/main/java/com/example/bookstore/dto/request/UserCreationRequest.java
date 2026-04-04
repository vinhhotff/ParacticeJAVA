package com.example.bookstore.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UserCreationRequest {
    @Size(min = 3, message = "Username phải từ 3 ký tự")
    String username;

    @Size(min = 8, message = "Password phải từ 8 ký tự")
    String password;

    String firstName;
    String lastName;
}
