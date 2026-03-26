package com.example.dropbox.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dropbox.dto.file.FileDto;
import com.example.dropbox.service.FileService;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileService fileService;

    @Autowired
    public FileController(FileService f) {
        fileService = f;
    }

    @GetMapping
    public ResponseEntity<List<FileDto>> getFiles(
        @CookieValue(name = "jwt") String jwtToken
    ){
        return ResponseEntity.ok().body(fileService.getUserFiles(jwtToken));
    }
}
