package com.example.dropbox.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dropbox.dto.file.FileDto;
import com.example.dropbox.exception.AuthException;
import com.example.dropbox.exception.ResourceNotFoundException;
import com.example.dropbox.model.FileMetadata;
import com.example.dropbox.model.Users;
import com.example.dropbox.repository.FileMetadataRepository;
import com.example.dropbox.repository.UsersRepository;


@Service
public class FileService {
    private final FileMetadataRepository fmdRepo;
    private final UsersRepository usersRepository;

    @Autowired
    public FileService(FileMetadataRepository f, UsersRepository u) {
        fmdRepo = f;
        usersRepository = u;

    }

    public List<FileDto> getUserFiles(String email) {
        Users user = usersRepository.findByEmail(email);
        List<FileMetadata> fileList = fmdRepo.findByUserId(user);
        return fileList.stream()
                .map(FileDto::fromEntity)
                .collect(Collectors.toList());
    }

    public FileDto getSharedFile(String token){
        FileMetadata file = fmdRepo.findByShareToken(UUID.fromString(token))
        .orElseThrow(()-> new ResourceNotFoundException("File not found"));
        return FileDto.fromEntity(file);
    }

    @Transactional
    public String delFile(String email, Long id) {
        Users user = usersRepository.findByEmail(email);
        fmdRepo.deleteByUserIdAndFileId(user, id);
        return "File: " + id + " Deleted success";
    }

    public List<FileDto> getFileByKeyword(String email, String keyword) {
        Users user = usersRepository.findByEmail(email);
        List<FileMetadata> fileList = fmdRepo.searchByKeyword(user.getId(), keyword);
        return fileList.stream()
                .map(FileDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public String genShareLink(String email,
            Long id) {
        Users user = usersRepository.findByEmail(email);
        FileMetadata file = fmdRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        if (!file.getUser().getId().equals(user.getId())) {
            throw new AuthException("Not authorized");
        }

        if (file.getShareToken() == null) {
            file.setShareToken(UUID.randomUUID());
            file.setIsShared(true);
            fmdRepo.save(file);
        }

        return "/api/share/" + file.getShareToken();
    }

    @Transactional
    public String delShareLink(String email, Long id) {
        Users user = usersRepository.findByEmail(email);
        FileMetadata file = fmdRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        if (!file.getUser().getId().equals(user.getId())) {
            throw new AuthException("Not authorized");
        }

        file.setShareToken(null);
        file.setIsShared(false);
        fmdRepo.saveAndFlush(file);
        return "Deleted Share Link! File name: " + file.getFileName();
    }
}
