package com.example.dropbox.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.dropbox.model.FileMetadata;
import com.example.dropbox.model.Users;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    Optional<FileMetadata> findByShareToken(UUID shareToken);

    List<FileMetadata> findByUser(Users user);

    @Query("SELECT DISTINCT f.objectKey FROM FileMetadata f WHERE f.objectKey IS NOT NULL")
    List<String> findDistinctObjectKey();

    Optional<FileMetadata> findByUserAndId(Users user, Long id);

    Optional<FileMetadata> findFirstByChecksum(String checksum);

    @Query(value = """
            SELECT * FROM file_metadata
        WHERE user_id = :userId
        AND (
            to_tsvector('english', extracted_text) @@ plainto_tsquery('english', :query) 
            OR file_name ILIKE '%' || :query || '%'
        )
        ORDER BY ts_rank(to_tsvector('english', extracted_text), plainto_tsquery('english', :query)) DESC
            """, nativeQuery = true)
    List<FileMetadata> searchByKeyword(@Param("userId") Long userId, @Param("query") String query);

    @Modifying
    @Transactional
    void deleteByUserAndId(Users user, Long id);

    @Modifying
    @Transactional
    @Query(value = """
        DELETE FROM file_metadata
        WHERE (status = 'FAILED' OR status = 'UPLOADING' OR status = 'PROCESSING') 
        AND created_at < :staleRecordsDaysLimit 
            """, nativeQuery = true)
    int deleteStaleRecords(LocalDateTime staleRecordsDaysLimit);
}
