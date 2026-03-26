package com.example.dropbox.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dropbox.dto.file.FileDto;
import com.example.dropbox.repository.FileMetadataRepository;

@Service
public class FileService {
    private final FileMetadataRepository fmdRepo;
    private final JwtUtil jwtUtil;

    @Autowired
    public FileService(FileMetadataRepository f, JwtUtil j) {
        fmdRepo = f;
        jwtUtil = j;

    }

    @Transactional
    public List<FileDto> getUserFiles(String jwtToken) {

    }
}
