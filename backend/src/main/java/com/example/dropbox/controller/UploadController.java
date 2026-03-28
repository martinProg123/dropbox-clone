package com.example.dropbox.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dropbox.dto.upload.UploadInitResponse;
import com.example.dropbox.exception.SizeLimtException;
import com.example.dropbox.service.UploadService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/upload")

@RequiredArgsConstructor
public class UploadController {
    private final UploadService uService;
    @Value("${app.file-size-limit}")
    private Long SIZELIMIT;

    // @Autowired
    // public UploadController(UploadService u) {
    //     uService = u;
    // }

    @PostMapping("/init")
    public ResponseEntity<UploadInitResponse> startUpload(
            @AuthenticationPrincipal String email,
            @RequestParam String fileName,
            @RequestParam Long fileSize) {
        if (fileSize > SIZELIMIT || fileSize <= 0)
            throw new SizeLimtException();
        if (fileName == null || fileName.isBlank())
            throw new IllegalArgumentException("Filename required");
        if (fileName.length() > 255)
            throw new IllegalArgumentException("Filename too long");
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\"))
            throw new IllegalArgumentException("Invalid filename");

        return ResponseEntity.ok().body(uService.start(email, fileName, fileSize));
    }

    @PostMapping("/complete")
    public ResponseEntity<String> endUpload(
            @AuthenticationPrincipal String email,
            @RequestParam Long fileId) {
        if(fileId <= 0 || fileId == null)
            throw new IllegalArgumentException("Invalid File ID");
        return ResponseEntity.ok().body(uService.complete(email, fileId));
    }
}
