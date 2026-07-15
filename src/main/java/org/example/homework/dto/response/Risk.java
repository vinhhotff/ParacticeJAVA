package org.example.homework.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Risk {
    private String type;
    private String severity;
    private String message;
}
