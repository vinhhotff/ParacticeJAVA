package com.example.bookstore.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PermissionResponse {
    String id;
    String description;
}
