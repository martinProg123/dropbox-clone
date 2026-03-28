package com.example.dropbox.service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dropbox.dto.file.FileProcessMsgDto;
import com.example.dropbox.dto.upload.UploadInitResponse;
import com.example.dropbox.exception.AuthException;
import com.example.dropbox.exception.ResourceNotFoundException;
import com.example.dropbox.exception.SizeLimtException;
import com.example.dropbox.model.FileMetadata;
import com.example.dropbox.model.UploadStatus;
import com.example.dropbox.model.Users;
import com.example.dropbox.repository.FileMetadataRepository;
import com.example.dropbox.repository.UsersRepository;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;

@Service
public class UploadService {
    private final FileMetadataRepository fmdRepo;
    private final UsersRepository usersRepository;
    private final MinioClient minioClient;
    private final FileMsgProducerService msgProducer;
    @Value("${minio.bucket}")
    private String bucket;
    @Value("${app.file-size-limit}")
    private Long SIZELIMIT;

    public UploadService(FileMetadataRepository fmdRepo, UsersRepository usersRepository, 
            MinioClient minioClient, FileMsgProducerService msgProducer) {
        this.fmdRepo = fmdRepo;
        this.usersRepository = usersRepository;
        this.minioClient = minioClient;
        this.msgProducer = msgProducer;
    }

    @Transactional
    public UploadInitResponse start(String email,
            String fileName,
            Long fileSize) {
        try {
            Users user = usersRepository.findByEmail(email);
            if (user == null) {
                throw new AuthException("User not found");
            }
            FileMetadata newFile = new FileMetadata();
            newFile.setFileName(fileName);
            newFile.setUser(user);
            String objectKey = "users/" + user.getId() + "/" + UUID.randomUUID();
            newFile.setObjectKey(objectKey);
            // newFile.setStatus(UploadStatus.UPLOADING);
            newFile = fmdRepo.save(newFile);
            String preSignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(15, TimeUnit.MINUTES)
                            .build());
            return new UploadInitResponse(newFile.getId().toString(), preSignedUrl);
        } catch (Exception e) {
            throw new RuntimeException(
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Upload failed");
        }
    }

    @Transactional
    public String complete(
            String email,
            Long fileId) {
        try {
            Users user = usersRepository.findByEmail(email);
            if (user == null) {
                throw new AuthException("User not found");
            }
            FileMetadata file = fmdRepo.findByUserAndId(user, fileId).orElseThrow();
            String objectKey = file.getObjectKey();

            long actualSize = checkFileSize(objectKey);
            if (actualSize > SIZELIMIT)
                throw new SizeLimtException();

            msgProducer.sendFileForProcessing(
                    new FileProcessMsgDto(
                            fileId,
                            objectKey));
            file.setStatus(UploadStatus.PROCESSING);
            file.setFileSize(actualSize);
            fmdRepo.save(file);
            return "Upload complete message received! Now processng";
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public long checkFileSize(String objectKey) throws InvalidKeyException, ErrorResponseException,
            InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException,
            ServerException, XmlParserException, IllegalArgumentException, IOException {
        StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .build());
        return stat.size();
    }
}
