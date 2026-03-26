package com.example.dropbox.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dropbox.dto.file.FileDto;
import com.example.dropbox.service.FileService;

@RestController
@RequestMapping("/api/share")
public class ShareController {
    private final FileService fileService;

    @Autowired
    public ShareController(FileService f) {
        fileService = f;
    }


    @GetMapping("/{token} ")
    public ResponseEntity<FileDto> getFiles(
        String token
    ){
        return ResponseEntity.ok().body(fileService.getSharedFile(token));
    }

    
}
