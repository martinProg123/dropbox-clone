package com.example.dropbox.service;

import com.example.dropbox.dto.file.FileDto;
import com.example.dropbox.exception.ResourceNotFoundException;
import com.example.dropbox.model.FileMetadata;
import com.example.dropbox.model.Users;
import com.example.dropbox.repository.FileMetadataRepository;
import com.example.dropbox.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileMetadataRepository fmdRepo;

    @Mock
    private UsersRepository usersRepository;

    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileService(fmdRepo, usersRepository, null);
    }

    @Test
    void getUserFiles_ShouldReturnUserFiles() {
        String email = "test@example.com";
        Users user = new Users();
        user.setId(1L);
        user.setEmail(email);

        FileMetadata file1 = new FileMetadata();
        file1.setId(1L);
        file1.setFileName("file1.txt");
        file1.setUser(user);

        when(usersRepository.findByEmail(email)).thenReturn(user);
        when(fmdRepo.findByUser(user)).thenReturn(List.of(file1));

        List<FileDto> result = fileService.getUserFiles(email);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("file1.txt", result.get(0).fileName());
    }

    @Test
    void isFileOwnedByUser_ShouldReturnTrue_WhenUserOwnsFile() {
        String email = "test@example.com";
        Long fileId = 1L;
        Users user = new Users();
        user.setId(1L);

        when(usersRepository.findByEmail(email)).thenReturn(user);
        when(fmdRepo.findByUserAndId(user, fileId)).thenReturn(Optional.of(new FileMetadata()));

        boolean result = fileService.isFileOwnedByUser(fileId, email);

        assertTrue(result);
    }

    @Test
    void isFileOwnedByUser_ShouldReturnFalse_WhenUserDoesNotOwnFile() {
        String email = "test@example.com";
        Long fileId = 1L;
        Users user = new Users();
        user.setId(1L);

        when(usersRepository.findByEmail(email)).thenReturn(user);
        when(fmdRepo.findByUserAndId(user, fileId)).thenReturn(Optional.empty());

        boolean result = fileService.isFileOwnedByUser(fileId, email);

        assertFalse(result);
    }

    @Test
    void getFileByKeyword_ShouldSearchFiles() {
        String email = "test@example.com";
        String keyword = "test";
        Users user = new Users();
        user.setId(1L);

        FileMetadata file = new FileMetadata();
        file.setId(1L);
        file.setFileName("test.txt");

        when(usersRepository.findByEmail(email)).thenReturn(user);
        when(fmdRepo.searchByKeyword(user.getId(), keyword)).thenReturn(List.of(file));

        List<FileDto> result = fileService.getFileByKeyword(email, keyword);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getSharedFile_ShouldReturnFile_WhenTokenIsValid() {
        UUID shareToken = UUID.randomUUID();
        String token = shareToken.toString();
        Users user = new Users();
        user.setEmail("owner@example.com");

        FileMetadata file = new FileMetadata();
        file.setId(1L);
        file.setFileName("shared.txt");
        file.setShareToken(shareToken);
        file.setUser(user);

        when(fmdRepo.findByShareToken(shareToken)).thenReturn(Optional.of(file));

        FileDto result = fileService.getSharedFile(token);

        assertNotNull(result);
        assertEquals("shared.txt", result.fileName());
    }

    @Test
    void getSharedFile_ShouldThrowException_WhenTokenNotFound() {
        UUID shareToken = UUID.randomUUID();

        when(fmdRepo.findByShareToken(shareToken)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> fileService.getSharedFile(shareToken.toString()));
    }
}