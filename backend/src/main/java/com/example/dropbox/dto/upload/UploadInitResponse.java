package com.example.dropbox.dto.upload;

public record UploadInitResponse(
    String fileId,
    String presignedUrl,
    boolean uploadNeeded
) {} 
