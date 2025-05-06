package com.imageProcessing.demo.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imageProcessing.demo.model.Image;


public interface imageRepo extends JpaRepository<Image,Integer> {

}
