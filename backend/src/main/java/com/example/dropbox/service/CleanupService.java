package com.example.dropbox.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.dropbox.config.JwtAuthenticationFilter;
import com.example.dropbox.repository.FileMetadataRepository;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;

@Service
public class CleanupService {
    private final FileMetadataRepository fmdRepo;
    private final MinioClient minioClient;
    @Value("${minio.bucket}")
    private String bucket;
    @Value("${cleanup.stale-days}")
    private int staleDays;
    private static final Logger log = LoggerFactory.getLogger(CleanupService.class);

    public CleanupService(FileMetadataRepository f, MinioClient m) {
        fmdRepo = f;
        minioClient = m;
    }

    public void cleanupOrphanObjects() {
        try {
            List<String> objectKeys = fmdRepo.findDistinctObjectKey();
            Set<String> dbObjectKeys = new HashSet<>(objectKeys);
            List<DeleteObject> delObjList = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix("obj/")
                            .recursive(false)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                if (!dbObjectKeys.contains(item.objectName()))
                    delObjList.add(new DeleteObject(item.objectName()));
            }

            if (!delObjList.isEmpty()) {
                Iterable<Result<DeleteError>> delResults  = minioClient.removeObjects(
                        RemoveObjectsArgs.builder().bucket(bucket).objects(delObjList).build());

                int errDelCnt = 0;
                for (Result<DeleteError> errorResult : delResults) {
                    DeleteError error = errorResult.get();
                    errDelCnt++;
                    System.out.println(
                            "Error in deleting object " + error.objectName() + "; " + error.message());
                }
                log.info("Deleted {} orphan objects", delObjList.size() - errDelCnt);
            }
            log.info("cleanupOrphanObjects Done.");
        } catch (Exception e) {
            log.error("cleanupOrphanObjects failed!");
            throw new RuntimeException(e.getMessage());
        }
    }

    public void cleanupStaleRecords() {
        try {
            LocalDateTime staleRecordsDaysLimit = LocalDateTime.now().minusDays(staleDays);
            int deletedCount = fmdRepo.deleteStaleRecords(staleRecordsDaysLimit);
            log.info("Deleted {} stale records", deletedCount);
        } catch (Exception e) {
            log.error("cleanupStaleRecords failed!");
            throw new RuntimeException(e.getMessage());
        }
    }

    @Scheduled(cron = "${cleanup.stale-cron:0 0 1 * * ?}")
    public void cleanupAll() {
        log.info("Ready to cleanupAll");
        cleanupStaleRecords();
        cleanupOrphanObjects();
        log.info("Finish cleanupAll");
    }

}
