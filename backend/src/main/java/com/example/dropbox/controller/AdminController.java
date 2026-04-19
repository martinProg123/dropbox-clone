package com.example.dropbox.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dropbox.service.CleanupService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final CleanupService cleanupService;
    private final Logger log = LoggerFactory.getLogger(AdminController.class);

    public AdminController(CleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @PostMapping("/cleanup")
    public void cleanup() {
        cleanupService.cleanupAll();
    }
}
