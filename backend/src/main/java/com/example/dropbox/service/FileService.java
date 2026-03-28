package com.example.dropbox.service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dropbox.dto.file.FileDto;
import com.example.dropbox.exception.AuthException;
import com.example.dropbox.exception.ResourceNotFoundException;
import com.example.dropbox.model.FileMetadata;
import com.example.dropbox.model.Users;
import com.example.dropbox.repository.FileMetadataRepository;
import com.example.dropbox.repository.UsersRepository;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class FileService {
    private final FileMetadataRepository fmdRepo;
    private final UsersRepository usersRepository;
    private final MinioClient minioClient;
    @Value("${minio.bucket}")
    private String bucket;

    // @Autowired
    // public FileService(FileMetadataRepository f, UsersRepository u) {
    // fmdRepo = f;
    // usersRepository = u;

    // }

    public List<FileDto> getUserFiles(String email) {
        Users user = usersRepository.findByEmail(email);
        List<FileMetadata> fileList = fmdRepo.findByUserId(user);
        return fileList.stream()
                .map(FileDto::fromEntity)
                .collect(Collectors.toList());
    }

    public FileDto getSharedFile(String token) {
        FileMetadata file = fmdRepo.findByShareToken(UUID.fromString(token))
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        return FileDto.fromEntity(file);
    }

    public boolean isFileOwnedByUser(Long id, String email) {
        Users user = usersRepository.findByEmail(email);
        Optional<FileMetadata> f = fmdRepo.findByUserIdAndFileId(user, id);
        return f.isPresent();
    }

    public String getPresignedDownloadLink(FileMetadata f) throws InvalidKeyException, ErrorResponseException,
            InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException,
            XmlParserException, ServerException, IllegalArgumentException, IOException {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .object(f.getObjectKey())
                        .expiry(15, TimeUnit.MINUTES)
                        .build());
    }

    public String downloadByUser(
            String email, Long id) throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException, XmlParserException, ServerException, IllegalArgumentException, IOException {
        Users user = usersRepository.findByEmail(email);
        Optional<FileMetadata> f = fmdRepo.findByUserIdAndFileId(user, id);
        return getPresignedDownloadLink(f.get());
    }

    public String downloadBySharedLink(String token) throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException, XmlParserException, ServerException, IllegalArgumentException, IOException {
        Optional<FileMetadata> f = fmdRepo.findByShareToken(UUID.fromString(token));
        return getPresignedDownloadLink(f.get());
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
