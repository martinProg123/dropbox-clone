package com.example.dropbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.dropbox.model.Users;

public interface UsersRepository extends JpaRepository<Users, Long>  {
    Users findByEmail(String email);
}
