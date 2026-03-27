package com.example.dropbox.worker;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.apache.tika.Tika;
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

import io.minio.GetObjectArgs;
import io.minio.MinioClient;

@Component
public class FileProcessWorker {

    private final FileMetadataRepository fmdRepo;
    private final MinioClient minioClient;
    private final Tika tika;
    @Value("${minio.bucket}")
    private String bucket;

    @Autowired
    public FileProcessWorker(FileMetadataRepository f, MinioClient m, Tika t) {
        fmdRepo = f;
        minioClient = m;
        tika = t;
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
            ByteArrayInputStream tikaStream = new ByteArrayInputStream(data);
            String text = tika.parseToString(tikaStream);

            ByteArrayInputStream mimeStream = new ByteArrayInputStream(data);
            String mimeType = tika.detect(mimeStream);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            byte[] hash = md.digest();

            file.setChecksum(HexFormat.of().formatHex(hash));
            file.setExtractedText(text);
            file.setStatus(UploadStatus.COMPLETED);
            file.setFileType(mimeType);
            file.setFileSize((long) data.length);
            fmdRepo.save(file);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            if(file != null)
                file.setStatus(UploadStatus.FAILED);
            fmdRepo.save(file);
            throw e;
        }
    }
}
