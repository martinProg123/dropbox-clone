package com.example.dropbox.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "file_metadata", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_share_token", columnList = "share_token")
})
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    private String url;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UploadStatus status = UploadStatus.UPLOADED;

    @Column(name = "is_shared")
    private Boolean isShared = false;

    @Column(name = "share_token", unique = true)
    private UUID shareToken;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
