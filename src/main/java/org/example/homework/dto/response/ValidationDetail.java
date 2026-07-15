package org.example.homework.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationDetail {
    private String field;
    private String message;
}
