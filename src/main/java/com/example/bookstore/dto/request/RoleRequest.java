package com.example.bookstore.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Value
@Builder
public class RoleRequest {
    String roleName;
     String description;

}
