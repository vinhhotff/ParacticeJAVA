package com.example.bookstore.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import java.util.Set;

@Jacksonized
@Value
@Builder
public class RoleRequest {
    String name;
    Set<String> permissions;
    String description;
}
