package com.example.bookstore.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthenticationResponse {
    String token;
    Boolean authenticated;
}
