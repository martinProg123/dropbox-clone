package com.example.dropbox.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.dropbox.dto.file.FileDto;
import com.example.dropbox.exception.AuthException;
import com.example.dropbox.service.FileService;
import com.example.dropbox.service.SseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;
    private final SseService sseService;

    // @Autowired
    // public FileController(FileService f) {
    //     fileService = f;
    // }

    @GetMapping
    public ResponseEntity<List<FileDto>> getFiles(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok().body(fileService.getUserFiles(email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFile(
            @AuthenticationPrincipal String email,
            @PathVariable Long id) {
        return ResponseEntity.ok().body(fileService.delFile(email, id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FileDto>> getFilesByFTSearch(
            @AuthenticationPrincipal String email,
            String keyword) {
        return ResponseEntity.ok().body(fileService.getFileByKeyword(email, keyword));
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<String> genShareLink(
            @AuthenticationPrincipal String email,
            @PathVariable Long id) {
        return ResponseEntity.ok().body(fileService.genShareLink(email, id));
    }

    @DeleteMapping("/{id}/share")
    public ResponseEntity<String> deleteShareLink(
            @AuthenticationPrincipal String email,
            @PathVariable Long id) {
        return ResponseEntity.ok().body(fileService.delShareLink(email, id));
    }

    @GetMapping("/{id}/events")
    public SseEmitter subscribe(
        @AuthenticationPrincipal String email,
        @PathVariable Long id) {
            if(!fileService.isFileOwnedByUser(id, email))
                throw new AuthException("Invalid access");
            return sseService.subscribe(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<String> downloadByUser(
        @AuthenticationPrincipal String email,
        @PathVariable Long id)  throws Exception {
        return ResponseEntity.ok().body(fileService.downloadByUser(email, id));
    }

}
