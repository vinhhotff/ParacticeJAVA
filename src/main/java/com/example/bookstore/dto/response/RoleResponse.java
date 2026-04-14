package com.example.bookstore.dto.response;

import lombok.Builder;
import lombok.Value;
import java.util.Set;

@Value
@Builder
public class RoleResponse {
    String name;
    String description;
    Set<PermissionResponse> permissions;
}
