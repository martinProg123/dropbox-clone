package com.example.dropbox.dto.upload;

public record UploadRequest(
        String fileName,
        Long fileSize,
        Long fileId
) {}
