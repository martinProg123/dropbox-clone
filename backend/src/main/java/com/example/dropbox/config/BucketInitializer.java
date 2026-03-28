package com.example.dropbox.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;

@Component
@Order(1)
public class BucketInitializer implements ApplicationRunner {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public BucketInitializer(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                System.out.println("Created bucket: " + bucket);
            } else {
                System.out.println("Bucket " + bucket + " already exists.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MinIO bucket: " + bucket, e);
        }
    }
}
