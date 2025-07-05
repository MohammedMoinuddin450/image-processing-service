package com.imageProcessing.demo.model;

import jakarta.persistence.GenerationType;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Builder;
import lombok.Data;

@Data
@Entity
@Builder
public class Image {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int imageId;
    private String filename;
    private String filetype;
    private Long fileSize;
    private String fileExtension;
    private int userid;
    private String r2url;
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

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}