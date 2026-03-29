package com.example.dropbox.service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
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
import java.security.NoSuchAlgorithmException;

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
            String fileType,
            Long fileSize,
            String checksum) {
        try {
            Users user = usersRepository.findByEmail(email);
            if (user == null) {
                throw new AuthException("User not found");
            }
            
            Optional<FileMetadata> existingFile = fmdRepo.findFirstByChecksum(checksum);
            if (existingFile.isPresent()) {
                FileMetadata newFile = new FileMetadata();
                newFile.setFileName(fileName);
                newFile.setFileType(fileType);
                newFile.setFileSize(fileSize);
                newFile.setUser(user);
                newFile.setChecksum(checksum); 
                newFile.setObjectKey(existingFile.get().getObjectKey());
                newFile.setStatus(UploadStatus.COMPLETED); 
                newFile = fmdRepo.save(newFile);
                return new UploadInitResponse(newFile.getId().toString(), null, false);
            }
            
            FileMetadata newFile = new FileMetadata();
            newFile.setFileName(fileName);
            newFile.setFileType(fileType);
            newFile.setFileSize(fileSize);
            newFile.setChecksum(checksum); 
            newFile.setUser(user);
            String objectKey = "obj/" + UUID.randomUUID();
            newFile.setObjectKey(objectKey);
            newFile.setStatus(UploadStatus.UPLOADING);
            newFile = fmdRepo.save(newFile);
            String preSignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(15, TimeUnit.MINUTES)
                            .build());
            return new UploadInitResponse(newFile.getId().toString(), preSignedUrl, true);
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
            return "Upload complete message received! Now processing";
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
