package com.example.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Thuộc tính nào null thì không hiện trong JSON
public class ApiResponse<T> {
    @Builder.Default
    int code = 1000; // Mặc định 1000 là thành công
    String message;
    T result;
}