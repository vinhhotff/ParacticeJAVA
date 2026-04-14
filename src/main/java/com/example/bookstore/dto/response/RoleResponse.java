package com.example.bookstore.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RoleResponse {
    String role;
    String description;
}
