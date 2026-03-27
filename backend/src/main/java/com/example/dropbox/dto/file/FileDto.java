package com.example.dropbox.dto.file;

import java.time.LocalDateTime;

import com.example.dropbox.model.FileMetadata;
import com.example.dropbox.model.UploadStatus;

public record FileDto(
        Long id,
        String fileName,
        String fileType,
        Long fileSize,
        LocalDateTime createdAt,
        UploadStatus status,
        String objectKey
    ) {
    public static FileDto fromEntity(FileMetadata file){
        return new FileDto(
                        file.getId(),
                        file.getFileName(),
                        file.getFileType(),
                        file.getFileSize(),
                        file.getCreatedAt(),
                        file.getStatus(),
                        file.getObjectKey()
                    );
    }
}