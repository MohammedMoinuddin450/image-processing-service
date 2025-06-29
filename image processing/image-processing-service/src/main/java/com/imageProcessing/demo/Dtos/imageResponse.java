package com.imageProcessing.demo.Dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class imageResponse {
    private int imageId;
    private String fileName;
    private String r2Url;
    private String fileType;
    private long fileSize;
}