package com.imageProcessing.demo.model;

import jakarta.persistence.GenerationType;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

@Data
@Entity
public class Image {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int imageId;
    private String filename;
    private String filetype;
    private int userid;
    private LocalDate createdDate;
    private LocalDate updatedDate;

    @PreUpdate
    public void onUpdate(){
        this.updatedDate=LocalDate.now();
    }

    @PrePersist
    public void onCreate(){
        this.createdDate=LocalDate.now();
    }
}
