package com.imageProcessing.demo.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.imageProcessing.demo.Dtos.imageResponse;
import com.imageProcessing.demo.service.imageService;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@RestController
@RequestMapping("/images")
@AllArgsConstructor
public class imageController {
    
    private imageService iService;

    @PostMapping("/addimage")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, Authentication authentication)
            throws IOException, S3Exception, AwsServiceException, SdkClientException {
        imageResponse response = iService.uploadImage(file, authentication);
        return ResponseEntity.ok().body(response+"image uploaded");
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<?> getImage(@PathVariable String id) {
        imageResponse imageData = iService.getImageData(id);
        return ResponseEntity.ok().body(imageData);
    }

    @DeleteMapping("/images/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable String id, Authentication authentication) throws IOException {
        iService.deleteImage(id, authentication);
        return ResponseEntity.ok().body("Image deleted successfully");
    }
}
