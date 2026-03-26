package com.example.dropbox.dto.file;

import java.time.LocalDateTime;

public record FileDto(
    Long id,
    String fileName,
    String fileType,
    Long fileSize,
    LocalDateTime createdAt
) {
} 