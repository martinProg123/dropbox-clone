package com.example.dropbox.repository;

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

    List<FileMetadata> findByUserId(Users users);

    FileMetadata findByUserIdAndFileId(Users user, Long id );

    @Query(value = """
            SELECT * FROM file_metadata
            WHERE user_id = :userId
            AND extracted_text @@ to_tsquery('english', :query)
            ORDER BY ts_rank(extracted_text, plainto_tsquery('english', :query)) DESC
            """, nativeQuery = true)
    List<FileMetadata> searchByKeyword(@Param("userId") Long userId, @Param("query") String query);

    @Modifying
    @Transactional
    void deleteByUserIdAndFileId(Users user, Long id );

}
