package com.example.dropbox.worker;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.dropbox.config.RabbitMQConfig;
import com.example.dropbox.dto.file.FileProcessMsgDto;
import com.example.dropbox.exception.ResourceNotFoundException;
import com.example.dropbox.model.FileMetadata;
import com.example.dropbox.model.UploadStatus;
import com.example.dropbox.repository.FileMetadataRepository;
import com.example.dropbox.service.SseService;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;

@Component
public class FileProcessWorker {

    private final FileMetadataRepository fmdRepo;
    private final MinioClient minioClient;
    private final Tika tika;
    private final SseService sseService;

    @Value("${minio.bucket}")
    private String bucket;

    public FileProcessWorker(FileMetadataRepository fmdRepo, MinioClient minioClient, Tika tika, SseService sseService) {
        this.fmdRepo = fmdRepo;
        this.minioClient = minioClient;
        this.tika = tika;
        this.sseService = sseService;
    }

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void receiveMessage(FileProcessMsgDto message) throws Exception {
        Long fileId = message.fileId();
        String objectKey = message.objectKey();
        if(fileId == null || objectKey == null) throw new ResourceNotFoundException("Invalid msg");
        FileMetadata file = fmdRepo.findById(fileId).orElseThrow(() -> new ResourceNotFoundException("No file found!"));
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .build())) {
            byte[] data = stream.readAllBytes();
            System.out.println("FileProcessWorker: Read " + data.length + " bytes for fileId " + fileId);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            byte[] hash = md.digest();
            String actualChecksum = HexFormat.of().formatHex(hash);
            
            System.out.println("FileProcessWorker: Actual checksum: " + actualChecksum);
            System.out.println("FileProcessWorker: Stored checksum: " + file.getChecksum());

            if (file.getChecksum() != null && !file.getChecksum().equals(actualChecksum)) {
                System.out.println("FileProcessWorker: Checksum mismatch! Attempting deduplication with actual checksum.");
                
                Optional<FileMetadata> existingFile = fmdRepo.findFirstByChecksum(actualChecksum);
                if (existingFile.isPresent()) {
                    System.out.println("FileProcessWorker: Found existing file with same checksum. Sharing object key.");
                    file.setObjectKey(existingFile.get().getObjectKey());
                    file.setChecksum(actualChecksum);
                    file.setExtractedText(existingFile.get().getExtractedText());
                    file.setFileType(existingFile.get().getFileType());
                    file.setFileSize(existingFile.get().getFileSize());
                } else {
                    System.out.println("FileProcessWorker: No existing file found with this checksum. Using actual checksum.");
                    file.setChecksum(actualChecksum);
                }
            } else if (file.getChecksum() == null) {
                System.out.println("FileProcessWorker: No stored checksum. Using actual checksum.");
                file.setChecksum(actualChecksum);
            }

            if (file.getExtractedText() == null || file.getExtractedText().isEmpty()) {
                System.out.println("FileProcessWorker: Processing file for text extraction.");
                ByteArrayInputStream mimeStream = new ByteArrayInputStream(data);
                String mimeType = tika.detect(mimeStream);
                System.out.println("FileProcessWorker: Detected MIME type: " + mimeType);

                String text; 
                ByteArrayInputStream tikaStream = new ByteArrayInputStream(data);
                Metadata metadata = new Metadata();
                metadata.set("resourceName", objectKey);
                metadata.set(Metadata.CONTENT_TYPE, mimeType);
                text = tika.parseToString(tikaStream, metadata);
                
                System.out.println("FileProcessWorker: Extracted text length: " + text.length() + " for fileId " + fileId);
                if (!text.isEmpty()) {
                    System.out.println("FileProcessWorker: First 100 chars: " + text.substring(0, Math.min(100, text.length())));
                }

                file.setExtractedText(text);
                file.setFileType(mimeType);
            }

            file.setFileSize((long) data.length);
            file.setStatus(UploadStatus.COMPLETED);
            fmdRepo.save(file);
            sseService.emit(fileId, "status", "COMPLETED");
        } catch (Exception e) {
            e.printStackTrace();
            if(file != null){
                file.setStatus(UploadStatus.FAILED);
                fmdRepo.save(file);
            }
            sseService.emit(fileId, "status", "FAILED");
            throw e;
        }
    }
}
