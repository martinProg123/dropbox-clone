package com.example.dropbox.dto.upload;

public record UploadRequest(
        String fileName,
        String fileType,
        Long fileSize,
        String checksum
) {}
