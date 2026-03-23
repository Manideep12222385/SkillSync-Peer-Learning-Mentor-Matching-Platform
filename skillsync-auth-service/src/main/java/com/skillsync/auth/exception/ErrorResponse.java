package com.skillsync.auth.exception;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private List<String> messages;
    private String path;
}