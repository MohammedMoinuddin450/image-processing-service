package com.imageProcessing.demo.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imageProcessing.demo.model.User;

public interface userRepo extends JpaRepository<User,Integer> {

    Optional<User> findByUserName(String username);


}
