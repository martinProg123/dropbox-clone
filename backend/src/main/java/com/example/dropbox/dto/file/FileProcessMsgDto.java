package com.example.dropbox.dto.file;

public record FileProcessMsgDto(
    Long fileId,
    String objectKey
) {}
