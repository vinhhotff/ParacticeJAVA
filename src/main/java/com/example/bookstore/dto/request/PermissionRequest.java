package com.example.bookstore.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Value
@Builder
public class PermissionRequest {
    String id;
    String description;
}
